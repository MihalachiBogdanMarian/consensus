package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Value;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class UC extends AbstractAlgorithm {

    private static Value val;
    private static boolean proposed;
    private static boolean decided;
    private static int ets; // current timestamp (of the current epoch)
    private static int newts; // the new epoch
    private static ProcessId newl; // the new leader

    public UC() {
        this.setName("UC");
    }

    @Override
    public boolean handle(Message message) {
        if (message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case UC_PROPOSE:
                    propose(message.getSystemId(),
                            message.getUcPropose().getValue().getV());
                    return true;
                case EC_START_EPOCH:
                    EcStartEpoch(message.getSystemId(),
                            message.getEcStartEpoch().getNewTimestamp(),
                            message.getEcStartEpoch().getNewLeader());
                    return true;
                case EP_ABORTED:
                    if (message.getEpAborted().getEts() == ets) {
                        EpAborted(message.getSystemId(),
                                message.getEpAborted().getEts(),
                                new EpState(message.getEpAborted().getValueTimestamp(), message.getEpAborted().getValue()));
                        return true;
                    } else {
                        return false;
                    }
                case EP_DECIDE:
                    if (message.getEpDecide().getEts() == ets) {
                        EpDecide(message.getSystemId(),
                                message.getEpAborted().getEts(),
                                message.getEpDecide().getValue().getV());
                        return true;
                    } else {
                        return false;
                    }
                case UC_DECIDE:
                    decide(message.getSystemId(),
                            message.getUcDecide().getValue().getV());
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public void init(String systemId) {
        this.displayExecution(systemId, "UcInit");

        val = Value.newBuilder().setDefined(false).build();
        proposed = false;
        decided = false;

        // obtain the leader ℓ0 of the initial epoch with timestamp 0 from epoch-change inst. ec
        // *** by calling before OmegaInit and EcInit ***

        // initialize a new instance ep.0 of epoch consensus with timestamp 0, leader ℓ0, and state (0,⊥)
        Process.systems.get(systemId).algorithms.put("EP.0", new EP(0, Process.systems.get(systemId).leader));

        ets = 0;
        Process.systems.get(systemId).leader = Utilities.maxrank(Process.processes);
        newts = 0;
        newl = null;

        Process.systems.get(systemId).algorithms.get("EP.0").init(systemId, new EpState(0, Value.newBuilder().setDefined(false).build()));
    }

    private void propose(String systemId, Integer v) {
        this.displayExecution(systemId, "UcPropose", v);
        val = Value.newBuilder().setDefined(true).setV(v).build();

//        specialMethod(systemId);
    }

    private void EcStartEpoch(String systemId, int newtsP, ProcessId newlP) {
        this.displayExecution(systemId, "EcStartEpoch", newtsP, newlP);
        newts = newtsP;
        newl = newlP;
        Process.systems.get(systemId).eventsQueue.add(
                Message.newBuilder()
                        .setType(Message.Type.EP_ABORT)
                        .setSystemId(systemId)
                        .setAbstractionId("ep" + ets)
                        .setEpAbort(Consensus.EpAbort.newBuilder()
                                .build())
                        .build()
        );
    }

    private void EpAborted(String systemId, int ts, EpState epState) {
        this.displayExecution(systemId, "Ep" + "." + ts + "Aborted", epState.getTimestamp(), epState.getValue().getV());
        ets = newts;
        Process.systems.get(systemId).leader = newl;
        proposed = false;

//        specialMethod(systemId);

        // initialize a new instance ep.ets of epoch consensus with timestamp ets,
        // leader ℓ, and state state;
        Process.systems.get(systemId).algorithms.put("EP." + ets, new EP(ets, Process.systems.get(systemId).leader));

        specialMethod(systemId);

        Process.systems.get(systemId).algorithms.get("EP." + ets).init(systemId, new EpState(epState.getTimestamp(), epState.getValue()));
    }

    private void specialMethod(String systemId) {
        if (Process.systems.get(systemId).leader.equals(Process.getSelf()) && val.getDefined() && !proposed) {
            this.displayExecution(systemId, "l=self^val!=null^proposed=false");
            proposed = true;
            Process.systems.get(systemId).eventsQueue.add(
                    Message.newBuilder()
                            .setType(Message.Type.EP_PROPOSE)
                            .setSystemId(systemId)
                            .setAbstractionId("ep" + ets)
                            .setEpPropose(Consensus.EpPropose.newBuilder()
                                    .setValue(Value.newBuilder().setDefined(true).setV(val.getV()).build())
                                    .build())
                            .build()
            );
        }
    }

    private void EpDecide(String systemId, int ts, int v) {
        this.displayExecution(systemId, "Ep" + "." + ts + "Decide", ts, v);
        if (!decided) {
            decided = true;
            Process.systems.get(systemId).eventsQueue.add(
                    Message.newBuilder()
                            .setType(Message.Type.UC_DECIDE)
                            .setSystemId(systemId)
                            .setAbstractionId("uc")
                            .setUcDecide(Consensus.UcDecide.newBuilder()
                                    .setValue(Value.newBuilder().setDefined(true).setV(v).build())
                                    .build())
                            .build()
            );
        }
    }

    private void decide(String systemId, int v) {
        this.displayExecution(systemId, "UcDecide", v);
        try {
            Process.hubSocket = new Socket(Process.HUB_ADDRESS, Process.HUB_PORT);
            OutputStream out = Process.hubSocket.getOutputStream();

            Utilities.writeMessage(out, Message.newBuilder()
                    .setType(Message.Type.NETWORK_MESSAGE)
                    .setSystemId(systemId)
                    .setAbstractionId("app")
                    .setNetworkMessage(
                            Consensus.NetworkMessage.newBuilder()
                                    .setSenderHost(Process.address)
                                    .setSenderListeningPort(Process.port)
                                    .setMessage(Message.newBuilder()
                                            .setSystemId(systemId)
                                            .setAbstractionId("app")
                                            .setType(Message.Type.APP_DECIDE)
                                            .setAppDecide(Consensus.AppDecide.newBuilder()
                                                    .setValue(Value.newBuilder().setDefined(true).setV(v).build())
                                                    .build())
                                            .build()
                                    ).build())
                    .build());

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Process.systems.get(systemId).stop = true;
                }
            }, 5000);

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

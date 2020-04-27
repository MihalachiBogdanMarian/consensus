package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class UC extends AbstractAlgorithm {

    private static Integer val;
    private static boolean proposed;
    private static boolean decided;
    private static int ets; // current timestamp (of the current epoch)
    private static ProcessId l; // the current leader (of the current epoch)
    private static int newts; // the new epoch
    private static ProcessId newl; // the new leader

    public UC() {
        this.setName("UC");
    }

    @Override
    public boolean handle(Message message) {
        switch (message.getType()) {
            case UC_PROPOSE:
                propose(Integer.parseInt(message.getSystemId()),
                        message.getUcPropose().getValue());
                return true;
            case EC_START_EPOCH:
                EcStartEpoch(Integer.parseInt(message.getSystemId()),
                        message.getEcStartEpoch().getNewTimestamp(),
                        message.getEcStartEpoch().getNewLeader());
                return true;
            case EP_ABORTED:
                if (Integer.parseInt(message.getAbstractionId()) == ets) {
                    EpAborted(Integer.parseInt(message.getSystemId()),
                            Integer.parseInt(message.getAbstractionId()),
                            new EpState(message.getEpAborted().getValueTimestamp(), message.getEpAborted().getValue()));
                    return true;
                } else {
                    return false;
                }
            case EP_DECIDE:
                if (Integer.parseInt(message.getAbstractionId()) == ets) {
                    EpDecide(Integer.parseInt(message.getSystemId()),
                            Integer.parseInt(message.getAbstractionId()),
                            message.getEpDecide().getValue());
                    return true;
                } else {
                    return false;
                }
            case UC_DECIDE:
                decide(Integer.parseInt(message.getSystemId()),
                        message.getUcDecide().getValue());
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void init(int systemId) {
        this.displayExecution(systemId, "UcInit");
        val = 0;
        proposed = false;
        decided = false;

        // obtain the leader ℓ0 of the initial epoch with timestamp 0 from epoch-change inst. ec
        // *** by calling before OmegaInit and EcInit ***

        // initialize a new instance ep.0 of epoch consensus with timestamp 0, leader ℓ0, and state (0,⊥)
        Process.systems.get(systemId).algorithms.put("EP.0", new EP(0, Process.systems.get(systemId).leader));
        Process.systems.get(systemId).eventsQueue.insert(
                Message.newBuilder()
                        .setType(Message.Type.EP_INIT)
                        .setSystemId(String.valueOf(systemId))
                        .setAbstractionId("0")
                        .setEpInit(Consensus.EpInit.newBuilder()
                                .setValueTimestamp(0)
                                .setValue(0)
                                .build())
                        .build()
        );

        ets = 0;
        Process.systems.get(systemId).leader = Process.systems.get(systemId).leader0;
        newts = 0;
        newl = null;
    }

    private void propose(int systemId, Integer v) {
        this.displayExecution(systemId, "UcPropose", v);
        val = v;

        specialMethod(systemId);
    }

    private void EcStartEpoch(int systemId, int newtsP, ProcessId newlP) {
        this.displayExecution(systemId, "EcStartEpoch", newtsP, newlP);
        newts = newtsP;
        newl = newlP;
        Process.systems.get(systemId).eventsQueue.insert(
                Message.newBuilder()
                        .setType(Message.Type.EP_ABORT)
                        .setSystemId(String.valueOf(systemId))
                        .setAbstractionId(String.valueOf(ets))
                        .setEpAbort(Consensus.EpAbort.newBuilder()
                                .build())
                        .build()
        );
    }

    private void EpAborted(int systemId, int ts, EpState epState) {
        this.displayExecution(systemId, "Ep" + "." + ts + "Aborted", epState.getTimestamp(), epState.getValue());
        ets = newts;
        Process.systems.get(systemId).leader = newl;
        proposed = false;

//        specialMethod(systemId);

        // initialize a new instance ep.ets of epoch consensus with timestamp ets,
        // leader ℓ, and state state;
        Process.systems.get(systemId).algorithms.put("EP." + ets, new EP(ets, Process.systems.get(systemId).leader));
        Process.systems.get(systemId).eventsQueue.insert(
                Message.newBuilder()
                        .setType(Message.Type.EP_INIT)
                        .setSystemId(String.valueOf(systemId))
                        .setAbstractionId(String.valueOf(ets))
                        .setEpInit(Consensus.EpInit.newBuilder()
                                .setValueTimestamp(epState.getTimestamp())
                                .setValue(epState.getValue())
                                .build())
                        .build()
        );

        specialMethod(systemId);
    }

    private void specialMethod(int systemId) {
        if (Process.systems.get(systemId).leader.equals(Process.getSelf()) && val != 0 && !proposed) {
            this.displayExecution(systemId, "l=self^val!=null^proposed=false");
            proposed = true;
            Process.systems.get(systemId).eventsQueue.insert(
                    Message.newBuilder()
                            .setType(Message.Type.EP_PROPOSE)
                            .setSystemId(String.valueOf(systemId))
                            .setAbstractionId(String.valueOf(ets))
                            .setEpPropose(Consensus.EpPropose.newBuilder()
                                    .setValue(val)
                                    .build())
                            .build()
            );
        }
    }

    private void EpDecide(int systemId, int ts, int v) {
        this.displayExecution(systemId, "Ep" + "." + ts + "Decide", ts, v);
        if (!decided) {
            decided = true;
            Process.systems.get(systemId).eventsQueue.insert(
                    Message.newBuilder()
                            .setType(Message.Type.UC_DECIDE)
                            .setSystemId(String.valueOf(systemId))
                            .setAbstractionId(String.valueOf(ts))
                            .setUcDecide(Consensus.UcDecide.newBuilder()
                                    .setValue(v)
                                    .build())
                            .build()
            );
        }
    }

    private void decide(int systemId, int v) {
        this.displayExecution(systemId, "UcDecide", v);
        try {
            Process.hubSocket = new Socket(Process.HUB_ADDRESS, Process.HUB_PORT);
            OutputStream out = Process.hubSocket.getOutputStream();

            Utilities.writeMessage(out,
                    Message.newBuilder()
                            .setType(Message.Type.APP_DECIDE)
                            .setSystemId(String.valueOf(systemId))
                            .setAppDecide(Consensus.AppDecide.newBuilder()
                                    .setValue(v)
                                    .setSender(Process.getSelf())
                                    .build())
                            .build());

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

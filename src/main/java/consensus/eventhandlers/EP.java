package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.Value;
import consensus.utilities.Utilities;

import java.util.HashMap;
import java.util.Map;

public class EP extends AbstractAlgorithm {

    private int ts; // instance for this epoch
    private ProcessId l;

    private static Integer valts;
    private static Value val;
    private static Value tmpval;
    private static Map<ProcessId, EpState> states;
    private static Integer accepted;
    private static boolean aborted;

    public EP(int ts, ProcessId l) {
        this.setName("EP." + ts);
        this.ts = ts;
        this.l = l;
    }

    @Override
    public boolean handle(Message message) {
        switch (message.getType()) {
            case EP_PROPOSE:
                if (message.getAbstractionId().equals("ep" + ts)) {
                    propose(message.getSystemId(),
                            message.getEpPropose().getValue());
                    return true;
                }
                return false;
            case BEB_DELIVER:
                if (message.getAbstractionId().equals("ep" + ts)) {
                    bebDeliver(message.getSystemId(),
                            message.getBebDeliver().getSender(),
                            message.getBebDeliver().getMessage());
                    return true;
                }
                return false;
            case PL_DELIVER:
                if (message.getAbstractionId().equals("ep" + ts)) {
                    plDeliver(message.getSystemId(),
                            message.getPlDeliver().getSender(),
                            message.getPlDeliver().getMessage());
                    return true;
                }
                return false;
            case EP_ABORT:
                if (message.getAbstractionId().equals("ep" + ts)) {
                    abort(message.getSystemId());
                    return true;
                }
                return false;
            default:
                break;
        }

        return false;
    }

    @Override
    public void init(String systemId, EpState state) {
        this.displayExecution(systemId, "EpInit");
        valts = state.getTimestamp();
        val = state.getValue();
        tmpval = Value.newBuilder().setDefined(false).build();
        states = new HashMap<>();
        for (int i = 0; i < Process.processes.size(); i++) {
            states.put(Process.processes.get(i), null);
        }
        accepted = 0;
        aborted = false;
    }

    private void propose(String systemId, Value v) {
        this.displayExecution(systemId, "EpPropose", v.getV());
        tmpval = v;
        Process.systems.get(systemId).eventsQueue.add(
                Message.newBuilder()
                        .setType(Message.Type.BEB_BROADCAST)
                        .setSystemId(systemId)
                        .setAbstractionId("beb")
                        .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                        .setType(Message.Type.EP_READ_)
                                        .setSystemId(systemId)
                                        .setAbstractionId("ep" + ts)
                                        .setEpRead(Consensus.EpRead_.newBuilder().build())
                                        .build()
                                ).build()
                        ).build()
        );
    }

    private void bebDeliver(String systemId, ProcessId processFrom, Message message) {
        this.displayExecution(systemId, "BebDeliver", processFrom, message);
        switch (message.getType()) {
            case EP_READ_:
                if (!aborted) {
                    Process.systems.get(systemId).eventsQueue.add(
                            Message.newBuilder()
                                    .setType(Message.Type.PL_SEND)
                                    .setSystemId(systemId)
                                    .setAbstractionId("ep" + ts)
                                    .setPlSend(Consensus.PlSend.newBuilder()
                                            .setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EP_STATE_)
                                                    .setSystemId(systemId)
                                                    .setAbstractionId("ep" + ts)
                                                    .setEpState(Consensus.EpState_.newBuilder()
                                                            .setValueTimestamp(valts)
                                                            .setValue(val)
                                                            .build())
                                                    .build())
                                            .setDestination(processFrom)
                                            .build())
                                    .build()
                    );
                }
                break;
            case EP_WRITE_:
                if (!aborted) {
                    valts = Integer.parseInt(message.getAbstractionId().split("ep")[1]);
                    val = message.getEpWrite().getValue();
                    Process.systems.get(systemId).eventsQueue.add(
                            Message.newBuilder()
                                    .setType(Message.Type.PL_SEND)
                                    .setSystemId(systemId)
                                    .setAbstractionId("ep" + ts)
                                    .setPlSend(Consensus.PlSend.newBuilder()
                                            .setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EP_ACCEPT_)
                                                    .setSystemId(systemId)
                                                    .setAbstractionId("ep" + ts)
                                                    .setEpAccept(Consensus.EpAccept_.newBuilder()
                                                            .build())
                                                    .build())
                                            .setDestination(processFrom)
                                            .build())
                                    .build()
                    );
                }
                break;
            case EP_DECIDED_:
                if (!aborted) {
                    Process.systems.get(systemId).eventsQueue.add(
                            Message.newBuilder()
                                    .setType(Message.Type.EP_DECIDE)
                                    .setSystemId(systemId)
                                    .setAbstractionId("uc")
                                    .setEpDecide(Consensus.EpDecide.newBuilder()
                                            .setEts(ts)
                                            .setValue(message.getEpDecided().getValue())
                                            .build())
                                    .build()
                    );
                }
                break;
            default:
                break;
        }
    }

    private void plDeliver(String systemId, ProcessId processFrom, Message message) {
        this.displayExecution(systemId, "PlDeliver", processFrom, message);
        switch (message.getType()) {
            case EP_STATE_:
                if (!aborted) {
                    states.put(processFrom,
                            new EpState(message.getEpState().getValueTimestamp(), message.getEpState().getValue()));
                    if (Utilities.hashtag(states) > Process.processes.size() / 2) {
                        EpState epState = Utilities.highest(states);

                        if (epState.getValue().getDefined()) {
                            tmpval = epState.getValue();
                        }

                        states = new HashMap<>();
                        for (int i = 0; i < Process.processes.size(); i++) {
                            states.put(Process.processes.get(i), null);
                        }

                        Process.systems.get(systemId).eventsQueue.add(
                                Message.newBuilder()
                                        .setType(Message.Type.BEB_BROADCAST)
                                        .setSystemId(systemId)
                                        .setAbstractionId("beb")
                                        .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                        .setType(Message.Type.EP_WRITE_)
                                                        .setSystemId(systemId)
                                                        .setAbstractionId("ep" + ts)
                                                        .setEpWrite(Consensus.EpWrite_.newBuilder().setValue(tmpval).build())
                                                        .build()
                                                ).build()
                                        ).build()
                        );
                    }
                }
                break;
            case EP_ACCEPT_:
                if (!aborted) {
                    accepted += 1;
                    if (accepted > Process.processes.size() / 2) {
                        accepted = 0;

                        Process.systems.get(systemId).eventsQueue.add(
                                Message.newBuilder()
                                        .setType(Message.Type.BEB_BROADCAST)
                                        .setSystemId(systemId)
                                        .setAbstractionId("beb")
                                        .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                        .setType(Message.Type.EP_DECIDED_)
                                                        .setSystemId(systemId)
                                                        .setAbstractionId("ep" + ts)
                                                        .setEpDecided(Consensus.EpDecided_.newBuilder().setValue(tmpval).build())
                                                        .build()
                                                ).build()
                                        ).build()
                        );
                    }
                }
                break;
            default:
                break;
        }
    }

    private void abort(String systemId) {
        this.displayExecution(systemId, "EpAbort", ts);

        Process.systems.get(systemId).eventsQueue.add(
                Message.newBuilder()
                        .setType(Message.Type.EP_ABORTED)
                        .setSystemId(systemId)
                        .setAbstractionId("ep" + ts)
                        .setEpAborted(Consensus.EpAborted.newBuilder()
                                .setEts(ts)
                                .setValueTimestamp(valts)
                                .setValue(val)
                                .build())
                        .build()
        );
        aborted = true;
    }

}

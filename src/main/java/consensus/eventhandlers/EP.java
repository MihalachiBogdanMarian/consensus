package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.util.HashMap;
import java.util.Map;

public class EP extends AbstractAlgorithm {

    private int ts; // instance for this epoch
    private ProcessId l;

    private static Integer valts;
    private static Integer val;
    private static Integer tmpval;
    private static Map<ProcessId, EpState> states;
    private static Integer accepted;
    private static boolean aborted;

    public EP(int ts, ProcessId l) {
        this.setName("EP");
        this.ts = ts;
        this.l = l;
    }

    @Override
    public boolean handle(Message message) {
        switch (message.getType()) {
            case EP_INIT:
                if (Integer.parseInt(message.getAbstractionId()) == ts) {
                    init(new EpState(message.getEpInit().getValueTimestamp(), message.getEpInit().getValue()));
                    return true;
                }
                return false;
            case EP_PROPOSE:
                if (Integer.parseInt(message.getAbstractionId()) == ts) {
                    propose(Integer.parseInt(message.getSystemId()),
                            message.getEpPropose().getValue());
                    return true;
                }
                return false;
            case BEB_DELIVER:
                if (message.getBebDeliver().getMessage().getType().equals(Message.Type.EP_READ_) ||
                        message.getBebDeliver().getMessage().getType().equals(Message.Type.EP_WRITE_) ||
                        message.getBebDeliver().getMessage().getType().equals(Message.Type.EP_DECIDED_)) {
                    if (Integer.parseInt(message.getBebDeliver().getMessage().getAbstractionId()) == ts) {
                        bebDeliver(Integer.valueOf(message.getSystemId()),
                                message.getBebDeliver().getSender(),
                                message.getBebDeliver().getMessage());
                        return true;
                    }
                    return false;
                }
                return false;
            case PL_DELIVER:
                if (message.getPlDeliver().getMessage().getType().equals(Message.Type.EP_STATE_) ||
                        message.getPlDeliver().getMessage().getType().equals(Message.Type.EP_ACCEPT_)) {
                    if (Integer.parseInt(message.getPlDeliver().getMessage().getAbstractionId()) == ts) {
                        plDeliver(Integer.valueOf(message.getSystemId()),
                                message.getPlDeliver().getSender(),
                                message.getPlDeliver().getMessage());
                        return true;
                    }
                    return false;
                }
                return false;
            case EP_ABORT:
                if (Integer.parseInt(message.getAbstractionId()) == ts) {
                    abort(Integer.parseInt(message.getSystemId()));
                    return true;
                }
                return false;
            default:
                break;
        }

        return false;
    }

    private void init(EpState state) {
        this.displayExecution("EpInit");
        valts = state.getTimestamp();
        val = state.getValue();
        tmpval = 0;
        states = new HashMap<>();
        for (int i = 0; i < Process.processes.size(); i++) {
            states.put(Process.processes.get(i), null);
        }
        accepted = 0;
        aborted = false;
    }

    private void propose(int systemId, int v) {
        this.displayExecution("EpPropose", v);
        tmpval = v;
        Process.systems.get(systemId).eventsQueue.insert(
                Message.newBuilder()
                        .setType(Message.Type.BEB_BROADCAST)
                        .setSystemId(String.valueOf(systemId))
                        .setAbstractionId(String.valueOf(ts))
                        .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                        .setType(Message.Type.EP_READ_)
                                        .setSystemId(String.valueOf(systemId))
                                        .setAbstractionId(String.valueOf(ts))
                                        .setEpRead(Consensus.EpRead_.newBuilder().build())
                                        .build()
                                ).build()
                        ).build()
        );
    }

    private void bebDeliver(int systemId, ProcessId processFrom, Message message) {
        this.displayExecution("BebDeliver", processFrom, message);
        switch (message.getType()) {
            case EP_READ_:
                if (!aborted) {
                    Process.systems.get(systemId).eventsQueue.insert(
                            Message.newBuilder()
                                    .setType(Message.Type.PL_SEND)
                                    .setSystemId(String.valueOf(systemId))
                                    .setAbstractionId(String.valueOf(ts))
                                    .setPlSend(Consensus.PlSend.newBuilder()
                                            .setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EP_STATE_)
                                                    .setSystemId(String.valueOf(systemId))
                                                    .setAbstractionId(String.valueOf(ts))
                                                    .setEpState(Consensus.EpState_.newBuilder()
                                                            .setValueTimestamp(valts)
                                                            .setValue(val)
                                                            .build())
                                                    .build())
                                            .setReceiver(processFrom)
                                            .build())
                                    .build()
                    );
                }
                break;
            case EP_WRITE_:
                if (!aborted) {
                    valts = Integer.parseInt(message.getAbstractionId());
                    val = message.getEpWrite().getValue();
                    Process.systems.get(systemId).eventsQueue.insert(
                            Message.newBuilder()
                                    .setType(Message.Type.PL_SEND)
                                    .setSystemId(String.valueOf(systemId))
                                    .setAbstractionId(String.valueOf(ts))
                                    .setPlSend(Consensus.PlSend.newBuilder()
                                            .setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EP_ACCEPT_)
                                                    .setSystemId(String.valueOf(systemId))
                                                    .setAbstractionId(String.valueOf(ts))
                                                    .setEpAccept(Consensus.EpAccept_.newBuilder()
                                                            .build())
                                                    .build())
                                            .setReceiver(processFrom)
                                            .build())
                                    .build()
                    );
                }
                break;
            case EP_DECIDED_:
                if (!aborted) {
                    Process.systems.get(systemId).eventsQueue.insert(
                            Message.newBuilder()
                                    .setType(Message.Type.EP_DECIDE)
                                    .setSystemId(String.valueOf(systemId))
                                    .setAbstractionId(String.valueOf(ts))
                                    .setEpDecide(Consensus.EpDecide.newBuilder()
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

    private void plDeliver(int systemId, ProcessId processFrom, Message message) {
        this.displayExecution("PlDeliver", processFrom, message);
        switch (message.getType()) {
            case EP_STATE_:
                if (!aborted) {
                    states.put(processFrom,
                            new EpState(message.getEpState().getValueTimestamp(), message.getEpState().getValue()));
                    if (Utilities.hashtag(states) > Process.processes.size() / 2) {
                        EpState epState = Utilities.highest(states);

                        if (epState.getValue() != 0) {
                            tmpval = epState.getValue();
                        }

                        states = new HashMap<>();
                        for (int i = 0; i < Process.processes.size(); i++) {
                            states.put(Process.processes.get(i), null);
                        }

                        Process.systems.get(systemId).eventsQueue.insert(
                                Message.newBuilder()
                                        .setType(Message.Type.BEB_BROADCAST)
                                        .setSystemId(String.valueOf(systemId))
                                        .setAbstractionId(String.valueOf(ts))
                                        .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                        .setType(Message.Type.EP_WRITE_)
                                                        .setSystemId(String.valueOf(systemId))
                                                        .setAbstractionId(String.valueOf(ts))
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

                        Process.systems.get(systemId).eventsQueue.insert(
                                Message.newBuilder()
                                        .setType(Message.Type.BEB_BROADCAST)
                                        .setSystemId(String.valueOf(systemId))
                                        .setAbstractionId(String.valueOf(ts))
                                        .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                        .setType(Message.Type.EP_DECIDED_)
                                                        .setSystemId(String.valueOf(systemId))
                                                        .setAbstractionId(String.valueOf(ts))
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

    private void abort(int systemId) {
        this.displayExecution("EpAbort", ts);

        Process.systems.get(systemId).eventsQueue.insert(
                Message.newBuilder()
                        .setType(Message.Type.EP_ABORTED)
                        .setSystemId(String.valueOf(systemId))
                        .setAbstractionId(String.valueOf(ts))
                        .setEpAborted(Consensus.EpAborted.newBuilder()
                                .setValueTimestamp(valts)
                                .setValue(val)
                                .build())
                        .build()
        );
        aborted = true;
    }

}

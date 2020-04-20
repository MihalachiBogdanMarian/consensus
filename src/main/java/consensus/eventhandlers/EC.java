package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

public class EC extends AbstractAlgorithm {

    private static ProcessId trusted;
    private static int lastts; // last epoch that it started
    private static int ts; // timestamp of an epoch at which it tried to be leader

    public EC() {
        this.setName("EC");
    }

    @Override
    public boolean handle(Message message) {
        switch (message.getType()) {
            case ELD_TRUST:
                omegaTrust(Integer.valueOf(message.getSystemId()),
                        message.getEldTrust().getProcessId());
                return true;
            case BEB_DELIVER:
                if (message.getBebDeliver().getMessage().getType().equals(Message.Type.EC_NEW_EPOCH_)) {
                    bebDeliver(Integer.valueOf(message.getSystemId()),
                            message.getBebDeliver().getSender(),
                            message.getBebDeliver().getMessage());
                    return true;
                }
                return false;
            case PL_DELIVER:
                if (message.getPlDeliver().getMessage().getType().equals(Message.Type.EC_NACK_)) {
                    plDeliver(Integer.valueOf(message.getSystemId()),
                            message.getPlDeliver().getSender(),
                            message.getPlDeliver().getMessage());
                    return true;
                }
                return false;
            default:
                break;
        }
        return false;
    }

    public void init(int systemId) {
        this.displayExecution("EcInit");
        trusted = Process.l0;
        lastts = 0;
        ts = Utilities.rank(Process.processes, Process.getSelf());
    }

    private void omegaTrust(int systemId, ProcessId process) {
        this.displayExecution("OmegaTrust", process);
        trusted = process;
        if (process.equals(Process.getSelf())) {
            ts += Process.processes.size();
            Process.systems.get(systemId).eventsQueue.insert(
                    Message.newBuilder()
                            .setType(Message.Type.BEB_BROADCAST)
                            .setSystemId(String.valueOf(systemId))
                            .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                            .setType(Message.Type.EC_NEW_EPOCH_)
                                            .setSystemId(String.valueOf(systemId))
                                            .setEcNewEpoch(Consensus.EcNewEpoch_.newBuilder().setTimestamp(ts).build())
                                            .build()
                                    ).build()
                            ).build()
            );
        }
    }

    private void bebDeliver(int systemId, ProcessId processFrom, Message message) {
        this.displayExecution("BebDeliver", processFrom, message);
        int newts = message.getEcNewEpoch().getTimestamp();
        switch (message.getType()) {
            case EC_NEW_EPOCH_:
                if (processFrom.equals(trusted) && newts > lastts) {
                    lastts = newts;
                    Process.systems.get(systemId).eventsQueue.insert(
                            Message.newBuilder()
                                    .setType(Message.Type.EC_START_EPOCH)
                                    .setSystemId(String.valueOf(systemId))
                                    .setEcStartEpoch(Consensus.EcStartEpoch.newBuilder()
                                            .setNewTimestamp(newts)
                                            .setNewLeader(processFrom)
                                            .build())
                                    .build()
                    );
                } else {
                    Process.systems.get(systemId).eventsQueue.insert(
                            Message.newBuilder()
                                    .setType(Message.Type.PL_SEND)
                                    .setSystemId(String.valueOf(systemId))
                                    .setPlSend(Consensus.PlSend.newBuilder()
                                            .setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EC_NACK_)
                                                    .setSystemId(String.valueOf(systemId))
                                                    .setEcNack(Consensus.EcNack_.newBuilder()
                                                            .build())
                                                    .build())
                                            .setReceiver(processFrom)
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
            case EC_NACK_:
                if (trusted.equals(Process.getSelf())) {
                    ts += Process.processes.size();
                    Process.systems.get(systemId).eventsQueue.insert(
                            Message.newBuilder()
                                    .setType(Message.Type.BEB_BROADCAST)
                                    .setSystemId(String.valueOf(systemId))
                                    .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EC_NEW_EPOCH_)
                                                    .setSystemId(String.valueOf(systemId))
                                                    .setEcNewEpoch(Consensus.EcNewEpoch_.newBuilder().setTimestamp(ts).build())
                                                    .build()
                                            ).build()
                                    ).build()
                    );
                }
                break;
            default:
                break;
        }
    }

}

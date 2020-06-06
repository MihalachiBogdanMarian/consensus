package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

public class EC extends AbstractAlgorithm {

    private static ProcessId trusted;
    private static int lastts; // last epoch that it started
    private static int ts; // timestamp of an epoch at which it tried to be leader

    public EC() {
        this.setName("EC");
    }

    @Override
    public boolean match(Message message) {
        if (message != null && message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case ELD_TRUST:
                    return true;
                case BEB_DELIVER:
                case PL_DELIVER:
                    if (message.getAbstractionId().equals("ec")) {
                        return true;
                    }
                    return false;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean handle(Message message) {
        if (message != null && message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case ELD_TRUST:
                    omegaTrust(message.getSystemId(),
                            message.getEldTrust().getProcess());
                    return true;
                case BEB_DELIVER:
                    if (message.getAbstractionId().equals("ec")) {
                        bebDeliver(message.getSystemId(),
                                message.getBebDeliver().getSender(),
                                message.getBebDeliver().getMessage());
                        return true;
                    }
                    return false;
                case PL_DELIVER:
                    if (message.getAbstractionId().equals("ec")) {
                        plDeliver(message.getSystemId(),
                                message.getPlDeliver().getSender(),
                                message.getPlDeliver().getMessage());
                        return true;
                    }
                    return false;
                default:
                    break;
            }
        }
        return false;
    }

    public void init(String systemId) {
        this.displayExecution(systemId, "EcInit");
        trusted = Process.systems.get(systemId).leader;
        lastts = 0;
        ts = Process.getSelf().getRank();
    }

    private void omegaTrust(String systemId, ProcessId process) {
        this.displayExecution(systemId, "OmegaTrust", process);
        trusted = process;
        if (process.equals(Process.getSelf())) {
            ts += Process.processes.size();
            Process.systems.get(systemId).eventsQueue.add(
                    Message.newBuilder()
                            .setType(Message.Type.BEB_BROADCAST)
                            .setSystemId(systemId)
                            .setAbstractionId("beb")
                            .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                            .setType(Message.Type.EC_NEW_EPOCH_)
                                            .setSystemId(systemId)
                                            .setAbstractionId("ec")
                                            .setEcNewEpoch(Consensus.EcNewEpoch_.newBuilder().setTimestamp(ts).build())
                                            .build()
                                    ).build()
                            ).build()
            );
        }
    }

    private void bebDeliver(String systemId, ProcessId processFrom, Message message) {
        this.displayExecution(systemId, "BebDeliver", processFrom, message);
        int newts = message.getEcNewEpoch().getTimestamp();
        switch (message.getType()) {
            case EC_NEW_EPOCH_:
                if (processFrom.equals(trusted) && newts > lastts) {
                    lastts = newts;
                    Process.systems.get(systemId).eventsQueue.add(
                            Message.newBuilder()
                                    .setType(Message.Type.EC_START_EPOCH)
                                    .setSystemId(systemId)
                                    .setAbstractionId("uc")
                                    .setEcStartEpoch(Consensus.EcStartEpoch.newBuilder()
                                            .setNewTimestamp(newts)
                                            .setNewLeader(processFrom)
                                            .build())
                                    .build()
                    );
                } else {
                    Process.systems.get(systemId).eventsQueue.add(
                            Message.newBuilder()
                                    .setType(Message.Type.PL_SEND)
                                    .setSystemId(systemId)
                                    .setAbstractionId("ec")
                                    .setPlSend(Consensus.PlSend.newBuilder()
                                            .setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EC_NACK_)
                                                    .setSystemId(systemId)
                                                    .setAbstractionId("ec")
                                                    .setEcNack(Consensus.EcNack_.newBuilder()
                                                            .build())
                                                    .build())
                                            .setDestination(processFrom)
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
            case EC_NACK_:
                if (trusted.equals(Process.getSelf())) {
                    ts += Process.processes.size();
                    Process.systems.get(systemId).eventsQueue.add(
                            Message.newBuilder()
                                    .setType(Message.Type.BEB_BROADCAST)
                                    .setSystemId(systemId)
                                    .setAbstractionId("beb")
                                    .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                    .setType(Message.Type.EC_NEW_EPOCH_)
                                                    .setSystemId(systemId)
                                                    .setAbstractionId("ec")
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

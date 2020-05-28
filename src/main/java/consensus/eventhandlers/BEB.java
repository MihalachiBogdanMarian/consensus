package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

public class BEB extends AbstractAlgorithm {

    public BEB() {
        this.setName("BEB");
    }

    @Override
    public boolean handle(Message message) {
        if (message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case BEB_BROADCAST:
                    broadcast(message.getSystemId(),
                            message.getBebBroadcast().getMessage());
                    return true;
                case PL_DELIVER:
                    if (message.getAbstractionId().equals("beb")) {
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

    private void broadcast(String systemId, Message message) {
        this.displayExecution(systemId, "BebBroadcast", message);
        for (ProcessId process : Process.processes) {
            Process.systems.get(systemId).eventsQueue.add(
                    Message.newBuilder()
                            .setType(Message.Type.PL_SEND)
                            .setSystemId(systemId)
                            .setAbstractionId("beb")
                            .setPlSend(Consensus.PlSend.newBuilder()
                                    .setMessage(message)
                                    .setDestination(process)
                                    .build())
                            .build()
            );
        }
    }

    private void plDeliver(String systemId, ProcessId processFrom, Message message) {
        this.displayExecution(systemId, "PlDeliver", processFrom, message);
        Process.systems.get(systemId).eventsQueue.add(
                Message.newBuilder()
                        .setType(Message.Type.BEB_DELIVER)
                        .setAbstractionId(message.getAbstractionId())
                        .setSystemId(systemId)
                        .setBebDeliver(Consensus.BebDeliver.newBuilder()
                                .setSender(processFrom)
                                .setMessage(message)
                                .build())
                        .build()
        );
    }

}

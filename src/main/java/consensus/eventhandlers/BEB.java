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
        switch (message.getType()) {
            case BEB_BROADCAST:
                broadcast(Integer.parseInt(message.getSystemId()),
                        message);
                return true;
            case PL_DELIVER:
                if (message.getPlDeliver().getMessage().getType().equals(Message.Type.BEB_BROADCAST)) {
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

    private void broadcast(int systemId, Message message) {
        this.displayExecution("BebBroadcast", message);
        for (ProcessId process : Process.processes) {
            Process.systems.get(systemId).eventsQueue.insert(
                    Message.newBuilder()
                            .setType(Message.Type.PL_SEND)
                            .setSystemId(String.valueOf(systemId))
                            .setPlSend(Consensus.PlSend.newBuilder()
                                    .setMessage(message)
                                    .setReceiver(process)
                                    .build())
                            .build()
            );
        }
    }

    private void plDeliver(int systemId, ProcessId processFrom, Message message) {
        this.displayExecution("PlDeliver", processFrom, message);
        switch (message.getType()) {
            case BEB_BROADCAST:
                Process.systems.get(systemId).eventsQueue.insert(
                        Message.newBuilder()
                                .setType(Message.Type.BEB_DELIVER)
                                .setSystemId(String.valueOf(systemId))
                                .setBebDeliver(Consensus.BebDeliver.newBuilder()
                                        .setSender(processFrom)
                                        .setMessage(message)
                                        .build())
                                .build()
                );
                break;
            default:
                break;
        }
    }

}

package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.EcNack_;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

public class BebDeliver extends AbstractEvent {

    private ProcessId processFrom;
    private Message message;

    public BebDeliver(ProcessId processFrom, Message message) {
        this.setName("BebDeliver");
        this.processFrom = processFrom;
        this.message = message;
    }

    @Override
    public void handle() {
        switch (message.getType()) {
            case EC_NEW_EPOCH_:
                if (processFrom.equals(Process.trusted) && Process.newts > Process.lastts) {
                    Process.lastts = Process.newts;
                    Process.eventsQueue.insert(new EcStartEpoch(Process.newts, Process.l));
                } else {
                    Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                            Message.newBuilder().setType(Message.Type.EC_NACK_).setEcNack(EcNack_.newBuilder().build()).build()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }
}

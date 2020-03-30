package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.EpState_;
import consensus.protos.Consensus.EcNack_;
import consensus.protos.Consensus.EpAccept_;
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
        this.displayExecution();
        switch (message.getType()) {
            case EC_NEW_EPOCH_:
                if (processFrom.equals(Process.trusted) && message.getEcNewEpoch().getTimestamp() > Process.lastts) {
                    Process.lastts = message.getEcNewEpoch().getTimestamp();
                    Process.eventsQueue.insert(new EcStartEpoch(message.getEcNewEpoch().getTimestamp(), processFrom));
                } else {
                    Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                            Message.newBuilder().setType(Message.Type.EC_NACK_).setEcNack(EcNack_.newBuilder().build()).build()));
                }
                break;
            case EP_READ_:
                Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                        Message.newBuilder().setType(Message.Type.EP_STATE_)
                                .setEpState(EpState_.newBuilder()
                                        .setValueTimestamp(Process.epInstances.get(message.getEpRead().getEpTimestamp()).getValts())
                                        .setValue(Process.epInstances.get(message.getEpRead().getEpTimestamp()).getVal())
                                        .setEpTimestamp(message.getEpRead().getEpTimestamp()).build())
                                .build()));
                break;
            case EP_WRITE_:
                Process.epInstances.get(message.getEpWrite().getEpTimestamp()).setValts(message.getEpWrite().getEpTimestamp());
                Process.epInstances.get(message.getEpWrite().getEpTimestamp()).setVal(message.getEpWrite().getValue());
                Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                        Message.newBuilder().setType(Message.Type.EP_ACCEPT_)
                                .setEpAccept(EpAccept_.newBuilder().setEpTimestamp(message.getEpWrite().getEpTimestamp()).build())
                                .build()));
                break;
            case EP_DECIDED_:
                Process.eventsQueue.insert(new EpDecide(message.getEpDecided().getEpTimestamp(), message.getEpDecided().getValue()));
                break;
            default:
                break;
        }
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (From: " + processFrom.toString().replace("\n", " ")
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }
}

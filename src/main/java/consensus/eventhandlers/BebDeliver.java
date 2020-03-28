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
            case EP_READ_:
                Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                        Message.newBuilder().setType(Message.Type.EP_STATE_)
                                .setEpState(EpState_.newBuilder()
                                        .setValueTimestamp(Process.epInstances.get(Process.ets).getValts())
                                        .setValue(Process.epInstances.get(Process.ets).getVal()).build())
                                .build()));
                break;
            case EP_WRITE_:
                Process.epInstances.get(Process.ets).setValts(Process.ets);
                Process.epInstances.get(Process.ets).setVal(message.getEpWrite().getValue());
                Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                        Message.newBuilder().setType(Message.Type.EP_ACCEPT_)
                                .setEpAccept(EpAccept_.newBuilder().build())
                                .build()));
                break;
            case EP_DECIDED_:
                Process.eventsQueue.insert(new EpDecide(Process.ets, message.getEpDecided().getValue()));
                break;
            default:
                break;
        }
    }
}

package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.EpState_;
import consensus.protos.Consensus.EcNack_;
import consensus.protos.Consensus.EpAccept_;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

import java.util.Collection;
import java.util.Collections;

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
        int ts;
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
                ts = Collections.max(Process.epInstances.keySet());
                if (!Process.epInstances.get(ts).isAborted()) {
                    Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                            Message.newBuilder().setType(Message.Type.EP_STATE_)
                                    .setEpState(EpState_.newBuilder()
                                            .setValue(Process.epInstances.get(ts).getVal())
                                            .build())
                                    .build()));
                }
                break;
            case EP_WRITE_:
                ts = Collections.max(Process.epInstances.keySet());
                if (!Process.epInstances.get(ts).isAborted()) {
                    Process.epInstances.get(ts).setValts(ts);
                    Process.epInstances.get(ts).setVal(message.getEpWrite().getValue());
                    Process.eventsQueue.insert(new PlSend(Process.getSelf(), processFrom,
                            Message.newBuilder().setType(Message.Type.EP_ACCEPT_)
                                    .setEpAccept(EpAccept_.newBuilder().build())
                                    .build()));
                }
                break;
            case EP_DECIDED_:
                ts = Collections.max(Process.epInstances.keySet());
                if (!Process.epInstances.get(ts).isAborted()) {
                    Process.eventsQueue.insert(new EpDecide(ts, message.getEpDecided().getValue()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean conditionFulfilled() {
        int ts = Collections.max(Process.epInstances.keySet());
        if (message.getType().equals(Message.Type.EP_READ_)) {
            if (!Process.epInstances.containsKey(ts)) {
                return false;
            }
            return true;
        } else if (message.getType().equals(Message.Type.EP_WRITE_)) {
            if (!Process.epInstances.containsKey(ts)) {
                return false;
            }
            return true;
        } else if (message.getType().equals(Message.Type.EP_DECIDED_)) {
            if (!Process.epInstances.containsKey(ts)) {
                return false;
            }
            return true;
        } else {
            return true;
        }
//        return true;
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (From: " + processFrom.getIndex()
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }
}

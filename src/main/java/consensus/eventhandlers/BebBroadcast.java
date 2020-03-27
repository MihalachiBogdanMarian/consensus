package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;

public class BebBroadcast extends AbstractEvent {

    private Message message;

    public BebBroadcast(Message message) {
        this.setName("BebBroadcast");
        this.message = message;
    }

    @Override
    public void handle() {
        for (Consensus.ProcessId process : Process.processes) {
            Process.eventsQueue.insert(new PlSend(Process.getSelf(), process, message));
        }
    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }
}

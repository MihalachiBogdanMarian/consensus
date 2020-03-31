package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

public class BebBroadcast extends AbstractEvent {

    private Message message;

    public BebBroadcast(Message message) {
        this.setName("BebBroadcast");
        this.message = message;
    }

    @Override
    public void handle() {
        this.displayExecution();
        for (ProcessId process : Process.processes) {
            Process.eventsQueue.insert(new PlSend(Process.getSelf(), process, message));
        }
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }
}

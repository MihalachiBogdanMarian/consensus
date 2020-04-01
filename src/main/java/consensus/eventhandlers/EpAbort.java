package consensus.eventhandlers;

import consensus.network.process.Process;

public class EpAbort extends AbstractEvent {

    private int ts; // instance for this epoch

    public EpAbort(int ts) {
        this.setName("EpAbort");
        this.ts = ts;
    }

    @Override
    public void handle() {
        this.displayExecution();
        Process.eventsQueue.insert(new EpAborted(ts, Process.epInstances.get(ts).getValts(), Process.epInstances.get(ts).getVal()));
        Process.epInstances.get(ts).setAborted(true);
    }

    @Override
    public boolean conditionFulfilled() {
        if (!Process.epInstances.containsKey(ts)) {
            return false;
        }
        return true;
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + "." + ts + " executing...");
        }
    }
}
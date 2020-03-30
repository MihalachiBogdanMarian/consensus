package consensus.eventhandlers;

import consensus.network.process.Process;

public class EpDecide extends AbstractEvent {

    private int ts; // instance for this epoch
    private int v; // value

    public EpDecide(int ts, int v) {
        this.setName("EpDecide");
        this.ts = ts;
        this.v = v;
    }

    @Override
    public void handle() {
        this.displayExecution();
        if (!Process.decided) {
            Process.decided = true;
            Process.eventsQueue.insert(new UcDecide(v));
        }
    }

    @Override
    public boolean conditionFulfilled() {
        return ts == Process.ets;
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + "." + ts + " (Value: " + v + ") executing...");
        }
    }
}
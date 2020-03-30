package consensus.eventhandlers;

import consensus.network.process.EpState;
import consensus.network.process.Process;

public class EpAborted extends AbstractEvent {

    private int ts; // instance for this epoch
    private int valts; // state timestamp
    private int val; // state value

    public EpAborted(int ts, int valts, int val) {
        this.setName("EpAborted");
        this.ts = ts;
        this.valts = valts;
        this.val = val;
    }

    @Override
    public void handle() {
        this.displayExecution();
        Process.ets = Process.newts;
        Process.l = Process.newl;
        Process.proposed = false;

//        if (Process.l.equals(Process.getSelf()) && Process.val != 0 && !Process.proposed) {
//            Process.proposed = true;
//            Process.eventsQueue.insert(new EpPropose(Process.ets, Process.l, new EpState(0, null), Process.val));
//        }

        // initialize a new instance ep.ets of epoch consensus with timestamp ets,
        // leader ℓ, and state state;
        Process.eventsQueue.insert(new EpInit(Process.ets, Process.l, new EpState(valts, val)));

//        if (Process.l.equals(Process.getSelf()) && Process.val != 0 && !Process.proposed) {
//            Process.proposed = true;
//            Process.eventsQueue.insert(new EpPropose(Process.ets, Process.l, new EpState(0, null), Process.val));
//        }
    }

    @Override
    public boolean conditionFulfilled() {
        return ts == Process.ets;
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + "." + ts + ".state(" + valts + ", " + val + ") executing...");
        }
    }
}
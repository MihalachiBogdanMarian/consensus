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
        Process.ets = Process.newts;
        Process.l = Process.newl;
        Process.proposed = false;
        // initialize a new instance ep.ets of epoch consensus with timestamp ets,
        // leader ℓ, and state state;
        Process.eventsQueue.insert(new EpInit(Process.ets, Process.l, new EpState(valts, val)));
        if (Process.l.equals(Process.getSelf()) && Process.val != null && !Process.proposed) {
            Process.proposed = true;
            Process.eventsQueue.insert(new EpPropose(Process.ets, Process.l, new EpState(0, null), Process.val));
        }
    }

    @Override
    public boolean conditionFulfilled() {
        return ts == Process.ets;
    }
}
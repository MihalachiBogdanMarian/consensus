package consensus.eventhandlers;

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
//        Initialize a new instance ep.ets of epoch consensus with timestamp ets,
//        leader ℓ, and state state;
    }

    @Override
    public boolean conditionFulfilled() {
        return ts == Process.ets;
    }
}
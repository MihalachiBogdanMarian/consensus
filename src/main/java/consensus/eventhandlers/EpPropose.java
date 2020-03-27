package consensus.eventhandlers;

import consensus.network.process.Process;

public class EpPropose extends AbstractEvent {

    private int ts; // instance for this epoch
    private int val;

    public EpPropose(int ts, int val) {
        this.setName("EpPropose");
        this.ts = ts;
        this.val = val;
    }

    @Override
    public void handle() {

    }

    @Override
    public boolean conditionFulfilled() {
        return ts == Process.ets;
    }
}

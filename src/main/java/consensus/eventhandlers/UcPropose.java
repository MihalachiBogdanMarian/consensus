package consensus.eventhandlers;

import consensus.network.process.Process;

public class UcPropose extends AbstractEvent {

    private Integer v;

    public UcPropose(Integer v) {
        this.setName("UcPropose");
        this.v = v;
    }

    @Override
    public void handle() {
        Process.val = v;
    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }
}
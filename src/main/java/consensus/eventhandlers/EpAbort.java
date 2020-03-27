package consensus.eventhandlers;

public class EpAbort extends AbstractEvent {

    private int ts; // instance for this epoch

    public EpAbort(int ts) {
        this.setName("EpAbort");
        this.ts = ts;
    }

    @Override
    public void handle() {

    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }
}
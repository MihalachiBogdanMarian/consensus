package consensus.eventhandlers;

import consensus.network.process.Process;

public class UcInit extends AbstractEvent {

    public UcInit() {
        this.setName("UcInit");
    }

    @Override
    public void handle() {
        Process.val = null;
        Process.proposed = false;
        Process.decided = false;

//        Obtain the leader ℓ0 of the initial epoch with timestamp 0 from epoch-change inst. ec;
//        Initialize a new instance ep.0 of epoch consensus with timestamp 0,
//                leader ℓ0, and state (0,⊥);
        Process.eventsQueue.insert(new EcInit());

        Process.ets = 0;
        Process.l = Process.l0;
        Process.newts = 0;
        Process.newl = null;
    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }
}

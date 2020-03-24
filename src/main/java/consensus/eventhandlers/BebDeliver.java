package consensus.eventhandlers;

import consensus.eventhandlers.AbstractEvent;

public class BebDeliver extends AbstractEvent {

    public BebDeliver() {
        this.setName("BebDeliver");
        this.setCondition(true);
    }

    @Override
    public void handle() {
        System.out.println(this.getClass().toString() + ": Handled!");
    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }
}

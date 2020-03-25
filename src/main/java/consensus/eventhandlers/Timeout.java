package consensus.eventhandlers;

public class Timeout extends AbstractEvent {

    public Timeout() {
        this.setName("Timeout");
        this.setCondition(true);
    }

    @Override
    public void handle() {

    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }
}
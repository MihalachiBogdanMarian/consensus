package consensus.eventhandlers;

public interface Event {

    String getName();

    void setName(String name);

    boolean getCondition();

    void setCondition(boolean condition);

    default void handle() {
        System.out.println(this.getClass().toString() + ": Handled!");
    }

    default void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }
}

package consensus.eventhandlers;

public interface Event {

    String getName();

    void setName(String name);

    boolean conditionFulfilled();

    default void handle() {
        System.out.println(this.getClass().toString() + ": Handled!");
    }
}

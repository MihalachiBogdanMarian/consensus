package consensus.eventhandlers;

public interface Event {

    String getName();

    void setName(String name);

    default boolean conditionFulfilled() {
        return true;
    }

    ;

    default void handle() {
        System.out.println(this.getClass().toString() + ": Handled!");
    }

    ;
}

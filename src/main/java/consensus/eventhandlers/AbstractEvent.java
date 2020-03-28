package consensus.eventhandlers;

public class AbstractEvent implements Event {

    protected String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AbstractEvent{" +
                "name='" + name + '\'' +
                '}';
    }
}
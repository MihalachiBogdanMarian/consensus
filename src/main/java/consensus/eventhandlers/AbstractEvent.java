package consensus.eventhandlers;

public class AbstractEvent implements Event {

    protected String name;
    protected boolean condition;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean getCondition() {
        return this.condition;
    }

    @Override
    public void setCondition(boolean condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "AbstractEvent{" +
                "name='" + name + '\'' +
                '}';
    }
}
package consensus.eventhandlers;

public class AbstractAlgorithm implements Algorithm {

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
        return "AbstractAlgorithm{" +
                "name='" + name + '\'' +
                '}';
    }
}

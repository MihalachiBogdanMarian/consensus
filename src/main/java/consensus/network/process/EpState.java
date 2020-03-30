package consensus.network.process;

public class EpState {

    private Integer timestamp;
    private Integer value;

    public EpState() {
        this.timestamp = -1;
        this.value = 0;
    }

    public EpState(Integer ts, Integer v) {
        this.timestamp = ts;
        this.value = v;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer ts) {
        this.timestamp = ts;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer v) {
        this.value = v;
    }
}

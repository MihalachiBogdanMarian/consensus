package consensus.eventhandlers;

import consensus.protos.Consensus.Value;

public class EpState {

    private Integer timestamp;
    private Value value;

    public EpState() {
        this.timestamp = -1;
        this.value = Value.newBuilder().setDefined(false).build();
    }

    public EpState(Integer ts, Value v) {
        this.timestamp = ts;
        this.value = v;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer ts) {
        this.timestamp = ts;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value v) {
        this.value = v;
    }
}

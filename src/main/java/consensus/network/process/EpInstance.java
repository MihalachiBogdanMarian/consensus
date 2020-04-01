package consensus.network.process;

import consensus.protos.Consensus.ProcessId;

import java.util.Map;

public class EpInstance {

    private Integer valts;
    private Integer val;
    private Integer tmpval;
    private Map<ProcessId, EpState> states;
    private Integer accepted;
    private boolean aborted;

    public EpInstance(Integer vals, Integer val, Integer tmpval, Map<ProcessId, EpState> states, Integer accepted) {
        this.valts = vals;
        this.val = val;
        this.tmpval = tmpval;
        this.states = states;
        this.accepted = accepted;
        this.aborted = false;
    }

    public Integer getValts() {
        return valts;
    }

    public void setValts(Integer vals) {
        this.valts = vals;
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }

    public Integer getTmpval() {
        return tmpval;
    }

    public void setTmpval(Integer tmpval) {
        this.tmpval = tmpval;
    }

    public Map<ProcessId, EpState> getStates() {
        return states;
    }

    public void setStates(Map<ProcessId, EpState> states) {
        this.states = states;
    }

    public Integer getAccepted() {
        return accepted;
    }

    public void setAccepted(Integer accepted) {
        this.accepted = accepted;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }
}

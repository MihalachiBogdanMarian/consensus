package consensus.eventhandlers;

import consensus.eventhandlers.AbstractEvent;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.EldHeartbeat_;

public class PlSend extends AbstractEvent {

    private ProcessId process;
    private EldHeartbeat_ eldHeartbeat_;

    public PlSend(ProcessId process, EldHeartbeat_ eldHeartbeat_) {
        this.setName("PlSend");
        this.setCondition(true);
        this.process = process;
        this.eldHeartbeat_ = eldHeartbeat_;
    }

    @Override
    public void handle() {

    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }
}

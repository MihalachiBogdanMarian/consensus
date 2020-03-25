package consensus.eventhandlers;

import consensus.protos.Consensus.ProcessId;

public class OmegaTrust extends AbstractEvent {

    private ProcessId leader;

    public OmegaTrust(ProcessId leader) {
        this.setName("OmegaTrust");
        this.setCondition(true);
        this.leader = leader;
    }

    @Override
    public void handle() {

    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }
}
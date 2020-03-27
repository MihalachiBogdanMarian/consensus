package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;

public class EcStartEpoch extends AbstractEvent {

    private int ts; // newts'
    private ProcessId process; // newl'

    public EcStartEpoch(int ts, ProcessId process) {
        this.setName("EcStartEpoch");
        this.ts = ts;
        this.process = process;
    }

    @Override
    public void handle() {
        Process.newts = ts;
        Process.newl = process;
        Process.eventsQueue.insert(new EpAbort(Process.ets));
    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }
}
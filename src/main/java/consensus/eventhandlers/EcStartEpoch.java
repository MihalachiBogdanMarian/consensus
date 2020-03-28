package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;

public class EcStartEpoch extends AbstractEvent {

    private int newtsP; // newts'
    private ProcessId newlP; // newl'

    public EcStartEpoch(int newtsP, ProcessId newlP) {
        this.setName("EcStartEpoch");
        this.newtsP = newtsP;
        this.newlP = newlP;
    }

    @Override
    public void handle() {
        Process.newts = newtsP;
        Process.newl = newlP;
        Process.eventsQueue.insert(new EpAbort(Process.ets));
    }
}
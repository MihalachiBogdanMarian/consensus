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
        this.displayExecution();
        Process.newts = newtsP;
        Process.newl = newlP;
        Process.eventsQueue.insert(new EpAbort(Process.ets));
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (New timestamp: " + newtsP + ", New leader: " + newlP.getIndex() + ") executing...");
        }
    }
}
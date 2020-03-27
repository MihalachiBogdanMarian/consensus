package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.utilities.Utilities;

public class EcInit extends AbstractEvent {

    public EcInit() {
        this.setName("EcInit");
    }

    @Override
    public void handle() {
        Process.eventsQueue.insert(new OmegaInit());
        Process.trusted = Process.l0;
        Process.lastts = 0;
        Process.ts = Utilities.rank(Process.processes, Process.port);
    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }
}
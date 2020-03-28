package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.utilities.Utilities;

import java.util.LinkedList;

public class OmegaInit extends AbstractEvent {

    public OmegaInit() {
        this.setName("OmegaInit");
    }

    @Override
    public void handle() {
        Process.epoch = 0;
        Utilities.store(Process.epoch, Process.fileName);
        Process.candidates = new LinkedList<>();
        Process.eventsQueue.insert(new OmegaRecovery());
    }
}
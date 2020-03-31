package consensus.eventhandlers;

import consensus.network.process.EpInstance;
import consensus.network.process.EpState;
import consensus.network.process.Process;
import consensus.protos.Consensus;

import java.util.HashMap;
import java.util.Map;

public class UcInit extends AbstractEvent {

    public UcInit() {
        this.setName("UcInit");
    }

    @Override
    public void handle() {
        this.displayExecution();
        Process.val = 0;
        Process.proposed = false;
        Process.decided = false;

        // obtain the leader ℓ0 of the initial epoch with timestamp 0 from epoch-change inst. ec
        // *** by calling before OmegaInit and EcInit ***
        // initialize a new instance ep.0 of epoch consensus with timestamp 0, leader ℓ0, and state (0,⊥)
//        Process.eventsQueue.insert(new EpInit(0, Process.l0, new EpState(0, 0)));
        Map<Consensus.ProcessId, EpState> epStates = new HashMap<>();
        for (int i = 0; i < Process.processes.size(); i++) {
            epStates.put(Process.processes.get(i), null);
        }
        Process.epInstances.put(0, new EpInstance(0, 0, 0, epStates, 0));

        Process.ets = 0;
        Process.l = Process.l0;
        Process.newts = 0;
        Process.newl = null;
    }
}

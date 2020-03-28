package consensus.eventhandlers;

import consensus.network.process.EpInstance;
import consensus.network.process.EpState;
import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;

import java.util.HashMap;
import java.util.Map;

public class EpInit extends AbstractEvent {

    private int ts; // instance for this epoch
    private ProcessId l;
    private EpState state;

    public EpInit(int ts, ProcessId l, EpState state) {
        this.setName("EpInit");
        this.ts = ts;
        this.l = l;
        this.state = state;
    }

    @Override
    public void handle() {
        Map<ProcessId, EpState> epStates = new HashMap<>();
        for (int i = 0; i < Process.processes.size(); i++) {
            epStates.put(Process.processes.get(i), null);
        }
        Process.epInstances.put(ts, new EpInstance(state.getTimestamp(), state.getValue(), null, epStates, 0));
    }
}

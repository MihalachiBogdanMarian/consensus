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
        this.displayExecution();
        Map<ProcessId, EpState> epStates = new HashMap<>();
        for (int i = 0; i < Process.processes.size(); i++) {
            epStates.put(Process.processes.get(i), null);
        }
        Process.epInstances.put(ts, new EpInstance(state.getTimestamp(), state.getValue(), 0, epStates, 0));

        if (Process.l.equals(Process.getSelf()) && Process.val != 0 && !Process.proposed) {
            Process.proposed = true;
            Process.eventsQueue.insert(new EpPropose(Process.ets, Process.l, new EpState(0, null), Process.val));
        }
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + "." + ts + " (Process: " + l.toString().replace("\n", " ")
                    + ", " + "State(" + state.getTimestamp() + ", " + state.getValue() + ")) executing...");
        }
    }
}

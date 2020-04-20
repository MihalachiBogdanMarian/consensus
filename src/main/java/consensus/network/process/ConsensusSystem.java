package consensus.network.process;

import consensus.eventhandlers.AbstractAlgorithm;
import consensus.eventsqueue.Queue;
import consensus.protos.Consensus.ProcessId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsensusSystem {

    public static int valueToPropose;
    public static List<ProcessId> processes = new ArrayList<>(); // the processes involved
    public static Map<String, AbstractAlgorithm> algorithms = new HashMap<>(); // the algorithms involved
    public static Queue eventsQueue = new Queue(); // defines the events

    public ConsensusSystem(int valueToPropose, List<ProcessId> processes, Map<String, AbstractAlgorithm> algorithms) {
        this.valueToPropose = valueToPropose;
        this.processes = processes;
        this.algorithms = algorithms;
    }

    public void eventLoop() {
        if (!eventsQueue.isEmpty()) {
            eventsQueue.deleteByCondition();
        }
    }
}

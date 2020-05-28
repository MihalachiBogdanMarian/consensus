package consensus.network.process;

import consensus.eventhandlers.AbstractAlgorithm;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsensusSystem {

    public static int valueToPropose;
    public static ProcessId leader;
    public static List<ProcessId> processes = new ArrayList<>(); // the processes involved
    public static Map<String, AbstractAlgorithm> algorithms = new HashMap<>(); // the algorithms involved
    public static List<Message> eventsQueue = new ArrayList<>(); // defines the events
    public boolean stop;

    public ConsensusSystem(int valueToPropose, List<ProcessId> processes, Map<String, AbstractAlgorithm> algorithms) {
        this.valueToPropose = valueToPropose;
        this.processes = processes;
        this.algorithms = algorithms;
        this.leader = null;
        this.stop = false;
    }

    public void eventLoop() {
        boolean b;
        List<Message> eventsQueueCopy = new ArrayList<>(eventsQueue);
        Map<String, AbstractAlgorithm> algorithmsCopy = new HashMap<>(algorithms);
        if (!eventsQueueCopy.isEmpty()) {
            for (Message message : eventsQueueCopy) {
                for (Map.Entry<String, AbstractAlgorithm> entry : algorithmsCopy.entrySet()) {
                    b = entry.getValue().handle(message);
                    if (b) {
                        eventsQueue.remove(message);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ConsensusSystem{" +
                "valueToPropose=" + valueToPropose +
                ", leader=" + leader +
                ", processes=" + processes +
                ", algorithms=" + algorithms +
                ", eventsQueue=" + eventsQueue +
                '}';
    }

}

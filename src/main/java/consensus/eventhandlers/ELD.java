package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.util.HashSet;
import java.util.Set;

public class ELD extends AbstractAlgorithm {

    private static Set<ProcessId> suspected;

    public ELD() {
        this.setName("ELD");
    }

    @Override
    public boolean match(Message message) {
        if (message != null && message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case EPFD_SUSPECT:
                case EPFD_RESTORE:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean handle(Message message) {
        if (message != null && message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case EPFD_SUSPECT:
                    suspect(message.getSystemId(),
                            message.getEpfdSuspect().getProcess());
                    return true;
                case EPFD_RESTORE:
                    restore(message.getSystemId(),
                            message.getEpfdRestore().getProcess());
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public void init(String systemId) {
        this.displayExecution(systemId, "EldInit");
        suspected = new HashSet<>();
        Process.systems.get(systemId).leader = null;
//        specialMethod(systemId);
    }

    private void suspect(String systemId, ProcessId p) {
        this.displayExecution(systemId, "EldSuspect");
        suspected.add(p);
        specialMethod(systemId);
    }

    private void restore(String systemId, ProcessId p) {
        this.displayExecution(systemId, "EldRestore");
        suspected.remove(p);
        specialMethod(systemId);
    }

    private void specialMethod(String systemId) {
        this.displayExecution(systemId, "EldSpecialMethod");
        ProcessId leader = Process.systems.get(systemId).leader;
        ProcessId maxrankLeaderWithoutSuspected = Utilities.maxrankWithoutSuspected(Process.processes, suspected);
        if (leader == null || !leader.equals(maxrankLeaderWithoutSuspected)) {
            leader = maxrankLeaderWithoutSuspected;
            if (leader != null) {
                Process.systems.get(systemId).leader = leader;
                Process.systems.get(systemId).eventsQueue.add(
                        Message.newBuilder()
                                .setType(Message.Type.ELD_TRUST)
                                .setSystemId(systemId)
                                .setAbstractionId("ec")
                                .setEldTrust(Consensus.EldTrust.newBuilder()
                                        .setProcess(Process.systems.get(systemId).leader)
                                        .build())
                                .build()
                );
            }
        }
    }

}

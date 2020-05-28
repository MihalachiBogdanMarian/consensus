package consensus.eventhandlers;

import com.google.common.collect.Sets;
import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

import java.util.*;

public class EPFD extends AbstractAlgorithm {

    private static Set<ProcessId> alive;
    private static Set<ProcessId> suspected;
    private static int delay;
    private static int delta = 100;

    public EPFD() {
        this.setName("EPFD");
    }

    @Override
    public boolean handle(Message message) {
        switch (message.getType()) {
            case EPFD_TIMEOUT:
                timeout(message.getSystemId());
                return true;
            case PL_DELIVER:
                if (message.getAbstractionId().equals("epfd")) {
                    plDeliver(message.getSystemId(),
                            message.getPlDeliver().getSender(),
                            message.getPlDeliver().getMessage());
                    return true;
                }
                return false;
            default:
                break;
        }
        return false;
    }

    @Override
    public void init(String systemId) {
        this.displayExecution(systemId, "EpfdInit");
        alive = new HashSet<>(Process.processes);
        suspected = new HashSet<>();
        delay = delta;
        starttimer(systemId, delay);
    }

    private void timeout(String systemId) {
        this.displayExecution(systemId, "EpfdTimeout");
        if (!Sets.intersection(alive, suspected).isEmpty()) {
            delay += delta;
        }

        for (ProcessId process : Process.processes) {
            if (!alive.contains(process) && !suspected.contains(process)) {
                suspected.add(process);
                Process.systems.get(systemId).eventsQueue.add(
                        Message.newBuilder()
                                .setType(Message.Type.EPFD_SUSPECT)
                                .setSystemId(systemId)
                                .setAbstractionId("eld")
                                .setEpfdSuspect(Consensus.EpfdSuspect.newBuilder().setProcess(process).build())
                                .build()
                );
            } else if (alive.contains(process) && suspected.contains(process)) {
                suspected.remove(process);
                Process.systems.get(systemId).eventsQueue.add(
                        Message.newBuilder()
                                .setType(Message.Type.EPFD_RESTORE)
                                .setSystemId(systemId)
                                .setAbstractionId("eld")
                                .setEpfdRestore(Consensus.EpfdRestore.newBuilder().setProcess(process).build())
                                .build()
                );
            }

            Process.systems.get(systemId).eventsQueue.add(
                    Message.newBuilder()
                            .setType(Message.Type.PL_SEND)
                            .setSystemId(systemId)
                            .setAbstractionId("epfd")
                            .setPlSend(Consensus.PlSend.newBuilder()
                                    .setMessage(Message.newBuilder()
                                            .setType(Message.Type.EPFD_HEARTBEAT_REQUEST)
                                            .setSystemId(systemId)
                                            .setAbstractionId("epfd")
                                            .setEpfdHeartbeatRequest(Consensus.EpfdHeartbeatRequest_.newBuilder().build())
                                            .build())
                                    .setDestination(process)
                                    .build())
                            .build()
            );
        }

        alive = new HashSet<>();
        starttimer(systemId, delay);
    }

    private void plDeliver(String systemId, ProcessId processFrom, Message message) {
        this.displayExecution(systemId, "PlDeliver", processFrom, message);
        switch (message.getType()) {
            case EPFD_HEARTBEAT_REQUEST:
                Process.systems.get(systemId).eventsQueue.add(
                        Message.newBuilder()
                                .setType(Message.Type.PL_SEND)
                                .setSystemId(systemId)
                                .setAbstractionId("epfd")
                                .setPlSend(Consensus.PlSend.newBuilder()
                                        .setMessage(Message.newBuilder()
                                                .setType(Message.Type.EPFD_HEARTBEAT_REPLY)
                                                .setSystemId(systemId)
                                                .setAbstractionId("epfd")
                                                .setEpfdHeartbeatReply(Consensus.EpfdHeartbeatReply_.newBuilder().build())
                                                .build())
                                        .setDestination(processFrom)
                                        .build())
                                .build()
                );
                break;
            case EPFD_HEARTBEAT_REPLY:
                alive.add(processFrom);
                break;
            default:
                break;
        }
    }

    private static void starttimer(String systemId, int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Process.systems.get(systemId).eventsQueue.add(
                        Message.newBuilder()
                                .setType(Message.Type.EPFD_TIMEOUT)
                                .setSystemId(systemId)
                                .setAbstractionId("epfd")
                                .setEpfdTimeout(Consensus.EpfdTimeout.newBuilder()
                                        .build())
                                .build()
                );
            }
        }, delay);
    }

}
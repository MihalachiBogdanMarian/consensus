package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class OMEGA extends AbstractAlgorithm {

    public static int epoch; // how many times the process crashed and recovered
    public static LinkedList<AbstractMap.SimpleEntry<ProcessId, Integer>> candidates;
    public final static int delta = 1000; // milliseconds
    public static int delay;

    public OMEGA() {
        this.setName("OMEGA");
    }

    @Override
    public boolean handle(Message message) {
        switch (message.getType()) {
            case ELD_RECOVERY:
                recovery(Integer.parseInt(message.getSystemId()));
                return true;
            case ELD_TIMEOUT:
                timeout(Integer.parseInt(message.getSystemId()));
                return true;
            case PL_DELIVER:
                if (message.getPlDeliver().getMessage().getType().equals(Message.Type.ELD_HEARTBEAT_)) {
                    plDeliver(Integer.valueOf(message.getSystemId()),
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

    public void init(int systemId) {
        this.displayExecution(systemId, "OmegaInit");
        epoch = 0;
        Utilities.store(epoch, Process.fileNames.get(systemId - 1));
        candidates = new LinkedList<>();
//        Process.systems.get(systemId).eventsQueue.insert(
//                Message.newBuilder()
//                        .setType(Message.Type.ELD_RECOVERY)
//                        .setSystemId(String.valueOf(systemId))
//                        .setEldRecovery(
//                                Consensus.EldRecovery.newBuilder()
//                                        .build()
//                        )
//                        .build()
//        );
        recovery(systemId);
    }

    private void recovery(int systemId) {
        this.displayExecution(systemId, "OmegaRecovery");
        Process.systems.get(systemId).leader0 = Utilities.maxrank(Process.processes);
        Process.systems.get(systemId).leader = Utilities.maxrank(Process.processes);

        Process.systems.get(systemId).eventsQueue.insert(
                Message.newBuilder()
                        .setType(Message.Type.ELD_TRUST)
                        .setSystemId(String.valueOf(systemId))
                        .setEldTrust(
                                Consensus.EldTrust.newBuilder()
                                        .setProcessId(Process.systems.get(systemId).leader)
                                        .build()
                        )
                        .build()
        );

        delay = delta;

        epoch = Utilities.retrieve(Process.fileNames.get(systemId - 1));
        epoch++;
        Utilities.store(epoch, Process.fileNames.get(systemId - 1));

        for (ProcessId process : Process.processes) {
            Process.systems.get(systemId).eventsQueue.insert(
                    Message.newBuilder()
                            .setType(Message.Type.PL_SEND)
                            .setSystemId(String.valueOf(systemId))
                            .setPlSend(Consensus.PlSend.newBuilder()
                                    .setMessage(Message.newBuilder()
                                            .setType(Message.Type.ELD_HEARTBEAT_)
                                            .setSystemId(String.valueOf(systemId))
                                            .setEldHeartbeat(Consensus.EldHeartbeat_.newBuilder()
                                                    .setEpoch(epoch)
                                                    .build())
                                            .build())
                                    .setReceiver(process)
                                    .build())
                            .build()
            );
        }

        candidates = new LinkedList<>();
        starttimer(systemId, delay);
    }

    private void timeout(int systemId) {
        this.displayExecution(systemId, "Timeout");
        ProcessId newLeader = Utilities.select(candidates);
        if (newLeader != null && !newLeader.equals(Process.systems.get(systemId).leader)) {
            delay += delta;
            Process.systems.get(systemId).leader = newLeader;
            Process.systems.get(systemId).eventsQueue.insert(
                    Message.newBuilder()
                            .setType(Message.Type.ELD_TRUST)
                            .setSystemId(String.valueOf(systemId))
                            .setEldTrust(
                                    Consensus.EldTrust.newBuilder()
                                            .setProcessId(Process.systems.get(systemId).leader)
                                            .build()
                            )
                            .build()
            );
        }

        for (ProcessId process : Process.processes) {
            Process.systems.get(systemId).eventsQueue.insert(
                    Message.newBuilder()
                            .setType(Message.Type.PL_SEND)
                            .setSystemId(String.valueOf(systemId))
                            .setPlSend(Consensus.PlSend.newBuilder()
                                    .setMessage(Message.newBuilder()
                                            .setType(Message.Type.ELD_HEARTBEAT_)
                                            .setSystemId(String.valueOf(systemId))
                                            .setEldHeartbeat(Consensus.EldHeartbeat_.newBuilder()
                                                    .setEpoch(epoch)
                                                    .build())
                                            .build())
                                    .setReceiver(process)
                                    .build())
                            .build()
            );
        }

        candidates = new LinkedList<>();
        starttimer(systemId, delay);
    }

    private void plDeliver(int systemId, ProcessId processFrom, Message message) {
        this.displayExecution(systemId, "PlDeliver", processFrom, message);
        switch (message.getType()) {
            case ELD_HEARTBEAT_:
                int exists = Utilities.exists(candidates, processFrom, message.getEldHeartbeat().getEpoch());
                if (exists != -1) {
                    candidates.remove(new AbstractMap.SimpleEntry<>(processFrom, exists));
                }
                Utilities.addInOrder(Process.processes, candidates, processFrom, message.getEldHeartbeat().getEpoch());
                break;
            default:
                break;
        }
    }

    private static void starttimer(int systemId, int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Process.systems.get(systemId).eventsQueue.insert(
                        Message.newBuilder()
                                .setType(Message.Type.ELD_TIMEOUT)
                                .setSystemId(String.valueOf(systemId))
                                .setEldTimeout(Consensus.EldTimeout.newBuilder()
                                        .build())
                                .build()
                );
            }
        }, delay);
    }

}

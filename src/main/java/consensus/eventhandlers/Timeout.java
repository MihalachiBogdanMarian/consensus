package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.EldHeartbeat_;

import consensus.utilities.Utilities;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Timeout extends AbstractEvent {

    public Timeout() {
        this.setName("Timeout");
    }

    @Override
    public void handle() {
        ProcessId newLeader = Utilities.select(Process.candidates);
        if (!newLeader.equals(Process.l)) {
            Process.delay += Process.delta;
            Process.l = newLeader;
            Process.eventsQueue.insert(new OmegaTrust(Process.l));
        }

        for (ProcessId process : Process.processes) {
            Process.eventsQueue.insert(
                    new PlSend(Process.getSelf(), process,
                            Message.newBuilder().setType(Message.Type.ELD_HEARTBEAT_).setEldHeartbeat(
                                    EldHeartbeat_.newBuilder().setEpoch(Process.epoch).build()
                            ).build()));
        }

        Process.candidates = new LinkedList<>();
        starttimer(Process.delay);
    }

    @Override
    public boolean conditionFulfilled() {
        return true;
    }

    private static void starttimer(int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Process.eventsQueue.insert(new Timeout());
            }
        }, delay * 1000);
    }
}
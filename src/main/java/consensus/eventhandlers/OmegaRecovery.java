package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.EldHeartbeat_;
import consensus.utilities.Utilities;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class OmegaRecovery extends AbstractEvent {

    public OmegaRecovery() {
        this.setName("OmegaRecovery");
    }

    @Override
    public void handle() {
        this.displayExecution();
        Process.l = Utilities.maxrank(Process.processes);

        Process.eventsQueue.insert(new OmegaTrust(Process.l));

        Process.delay = Process.delta;

        Process.epoch = Utilities.retrieve(Process.fileName);
        Process.epoch++;
        Utilities.store(Process.epoch, Process.fileName);

        for (ProcessId process : Process.processes) {
            Process.eventsQueue.insert(
                    new PlSend(Process.getSelf(), process,
                            Message.newBuilder().setType(Message.Type.ELD_HEARTBEAT_)
                                    .setEldHeartbeat(EldHeartbeat_.newBuilder()
                                            .setEpoch(Process.epoch).build())
                                    .build()
                    )
            );
        }

        Process.candidates = new LinkedList<>();
        starttimer(Process.delay);
    }

    private static void starttimer(int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Process.eventsQueue.insert(new Timeout());
            }
        }, delay);
    }
}
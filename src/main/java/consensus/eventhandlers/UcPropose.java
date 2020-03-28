package consensus.eventhandlers;

import consensus.network.process.EpState;
import consensus.network.process.Process;

public class UcPropose extends AbstractEvent {

    private Integer v;

    public UcPropose(Integer v) {
        this.setName("UcPropose");
        this.v = v;
    }

    @Override
    public void handle() {
        Process.val = v;
        if (Process.l.equals(Process.getSelf()) && Process.val != null && !Process.proposed) {
            Process.proposed = true;
            Process.eventsQueue.insert(new EpPropose(Process.ets, Process.l, new EpState(0, null), Process.val));
        }
    }
}
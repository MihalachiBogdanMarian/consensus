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
        this.displayExecution();
        Process.val = v;

//        if (Process.l.equals(Process.getSelf()) && Process.val != 0 && !Process.proposed) {
//            Process.proposed = true;
//            Process.eventsQueue.insert(new EpPropose(Process.ets, Process.l, new EpState(0, null), Process.val));
//        }
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (Value: " + v + ") executing...");
        }
    }
}
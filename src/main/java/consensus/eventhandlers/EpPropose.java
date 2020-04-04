package consensus.eventhandlers;

import consensus.network.process.EpState;
import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.EpRead_;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;

public class EpPropose extends AbstractEvent {

    private int ts; // instance for this epoch
    private ProcessId l;
    private EpState state;
    private int v;

    public EpPropose(int ts, ProcessId l, EpState state, int v) {
        this.setName("EpPropose");
        this.ts = ts;
        this.l = l;
        this.state = state;
        this.v = v;
    }

    @Override
    public void handle() {
        this.displayExecution();
        if (!Process.epInstances.get(ts).isAborted()) {
            Process.epInstances.get(ts).setTmpval(v);

            Process.eventsQueue.insert(new BebBroadcast(
                    Message.newBuilder().setType(Message.Type.BEB_BROADCAST)
                            .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                            .setType(Message.Type.EP_READ_)
                                            .setEpRead(EpRead_.newBuilder().build())
                                            .build()
                                    ).build()
                            ).build()
            ));
        }
    }

    @Override
    public void displayExecution() {
        System.out.println(super.getName() + "." + ts + " (Value: " + v + ") executing...");
    }
}

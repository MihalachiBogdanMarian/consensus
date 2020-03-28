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
    private int val;

    public EpPropose(int ts, ProcessId l, EpState state, int val) {
        this.setName("EpPropose");
        this.ts = ts;
        this.l = l;
        this.state = state;
        this.val = val;
    }

    @Override
    public void handle() {
        Process.epInstances.get(ts).setTmpval(val);

        Process.eventsQueue.insert(new consensus.eventhandlers.BebBroadcast(
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

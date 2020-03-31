package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.BebBroadcast;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.EcNewEpoch_;

public class OmegaTrust extends AbstractEvent {

    private ProcessId process;

    public OmegaTrust(ProcessId process) {
        this.setName("OmegaTrust");
        this.process = process;
    }

    @Override
    public void handle() {
        this.displayExecution();
        Process.trusted = process;
        if (process.equals(Process.getSelf())) {
            Process.ts += Process.processes.size();
            Process.eventsQueue.insert(new consensus.eventhandlers.BebBroadcast(
                    Message.newBuilder().setType(Message.Type.BEB_BROADCAST)
                            .setBebBroadcast(BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                            .setType(Message.Type.EC_NEW_EPOCH_)
                                            .setEcNewEpoch(EcNewEpoch_.newBuilder().setTimestamp(Process.ts).build())
                                            .build()
                                    ).build()
                            ).build()
            ));
        }
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (Trusted process: " + process.getIndex() + ") executing...");
        }
    }
}
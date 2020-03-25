package consensus.eventhandlers;

import consensus.network.Client;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.EldHeartbeat_;

import java.util.ArrayList;
import java.util.List;

public class OmegaRecovery extends AbstractEvent {

    public OmegaRecovery() {
        this.setName("OmegaRecovery");
        this.setCondition(true);
    }

    @Override
    public void handle() {
        ProcessId leader = maxrank(Client.processes);

        Client.eventsQueue.insert(new OmegaTrust(leader));

        Client.delay = Client.delta;

        Client.epoch = Client.retrieve();
        Client.epoch++;
        Client.store(Client.epoch);

        for (ProcessId process : Client.processes) {
            Client.eventsQueue.insert(new PlSend(process, EldHeartbeat_.newBuilder().setEpoch(Client.epoch).build()));
        }

        Client.candidates = new ArrayList<>();
        Client.starttimer(Client.delay);
    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }

    private static ProcessId maxrank(List<ProcessId> processes) {
        ProcessId maxLeader = processes.get(0);
        for (ProcessId process : processes) {
            if (process.getIndex() > maxLeader.getIndex()) {
                maxLeader = process;
            }
        }
        return maxLeader;
    }
}
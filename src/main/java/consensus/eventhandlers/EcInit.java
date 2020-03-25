package consensus.eventhandlers;

import consensus.network.Client;
import consensus.protos.Consensus.ProcessId;

public class EcInit extends AbstractEvent {

    public EcInit() {
        this.setName("EcInit");
        this.setCondition(true);
    }

    @Override
    public void handle() {
        Client.eventsQueue.insert(new OmegaInit());
        Client.trusted = Client.l0;
        Client.lastts = 0;
        Client.ts = rank(Client.port);
    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }

    private static int rank(int self) {
        for (ProcessId processId : Client.processes) {
            if (processId.getPort() == self) {
                return processId.getIndex();
            }
        }
        return 0;
    }
}
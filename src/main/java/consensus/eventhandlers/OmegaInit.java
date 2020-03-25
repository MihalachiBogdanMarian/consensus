package consensus.eventhandlers;

import consensus.network.Client;

import java.util.ArrayList;

public class OmegaInit extends AbstractEvent {

    public OmegaInit() {
        this.setName("OmegaInit");
        this.setCondition(true);
    }

    @Override
    public void handle() {
        Client.epoch = 0;
        Client.store(Client.epoch);
        Client.candidates = new ArrayList<>();
        Client.eventsQueue.insert(new OmegaRecovery());
    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }
}
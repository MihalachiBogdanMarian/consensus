package consensus.eventhandlers;

import consensus.network.Client;

public class UcInit extends AbstractEvent {

    public UcInit() {
        this.setName("UcInit");
        this.setCondition(true);
    }

    @Override
    public void handle() {
        Client.val = 0;
        Client.proposed = false;
        Client.decided = false;

        Client.eventsQueue.insert(new EcInit());

        Client.ets = 0;
        Client.l = Client.l0;
        Client.newts = 0;
        Client.newl = null;
    }

    @Override
    public void match() {
        System.out.println(this.getClass().toString() + ": It's a match!");
    }
}

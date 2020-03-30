package consensus.eventhandlers;

import consensus.eventsqueue.Queue;
import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.io.OutputStream;

public class UcDecide extends AbstractEvent {

    private int v; // value

    public UcDecide(int v) {
        this.setName("UcDecide");
        this.v = v;
    }

    @Override
    public void handle() {
        this.displayExecution();
        try {
            OutputStream out = Process.socket.getOutputStream();

            Utilities.writeProcess(out, Process.getSelf());
            Utilities.writeProcess(out, Process.getSelf());
            Utilities.writeMessage(out, Message.newBuilder().
                    setType(Message.Type.UC_DECIDE).
                    setUcDecide(Consensus.UcDecide.newBuilder().setValue(v).build())
                    .build());

            out.flush();

            Process.eventsQueue = new Queue<>(); // deplete the events queue
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (Value: " + v + ") executing...");
        }
    }
}
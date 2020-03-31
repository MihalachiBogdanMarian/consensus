package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.io.OutputStream;

public class PlSend extends AbstractEvent {

    private ProcessId processFrom;
    private ProcessId processTo;
    private Message message;

    public PlSend(ProcessId processFrom, ProcessId processTo, Message message) {
        this.setName("PlSend");
        this.processFrom = processFrom;
        this.processTo = processTo;
        this.message = message;
    }

    @Override
    public void handle() {
        this.displayExecution();
        try {
            OutputStream out = Process.socket.getOutputStream();

            Utilities.writeProcess(out, processFrom);
            Utilities.writeProcess(out, processTo);
            Utilities.writeMessage(out, message);

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayExecution() {
        synchronized (System.out) {
            System.out.println(super.getName() + " (From: " + processFrom.getIndex()
                    + ", To: " + processTo.getIndex()
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }
}

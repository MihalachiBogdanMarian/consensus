package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.net.Socket;

public class PL extends AbstractAlgorithm {

    public PL() {
        this.setName("PL");
    }

    @Override
    public boolean handle(Message message) {
        switch (message.getType()) {
            case PL_SEND:
                send(message.getPlSend().getReceiver(),
                        message.getPlSend().getMessage());
                return true;
            default:
                break;
        }
        return false;
    }

    private void send(ProcessId processTo, Message message) {
        this.displayExecution("PlSend", Process.getSelf(), processTo, message);
        Socket socket = null;
        try {
            socket = new Socket(processTo.getHost(), processTo.getPort());

            Utilities.writeMessage(socket.getOutputStream(), message);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

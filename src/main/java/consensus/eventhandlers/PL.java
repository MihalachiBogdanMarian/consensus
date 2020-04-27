package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
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
                if (message.getPlSend().getMessage().getType().equals(Message.Type.EP_STATE_) ||
                        message.getPlSend().getMessage().getType().equals(Message.Type.EP_ACCEPT_)
                ) {
                    send(Integer.parseInt(message.getSystemId()),
                            Integer.parseInt(message.getPlSend().getMessage().getAbstractionId()),
                            message.getPlSend().getReceiver(),
                            message.getPlSend().getMessage());
                } else {
                    send(Integer.parseInt(message.getSystemId()),
                            message.getPlSend().getReceiver(),
                            message.getPlSend().getMessage());
                }
                return true;
            default:
                break;
        }
        return false;
    }

    private void send(int systemId, int abstractionId, ProcessId processTo, Message message) {
        this.displayExecution(systemId, "PlSend", Process.getSelf(), processTo, message);
        Socket socket = null;
        try {
            socket = new Socket(processTo.getHost(), processTo.getPort());

            Utilities.writeMessage(socket.getOutputStream(),
                    Message.newBuilder()
                            .setType(Message.Type.PL_DELIVER)
                            .setSystemId(String.valueOf(systemId))
                            .setAbstractionId(String.valueOf(abstractionId))
                            .setPlDeliver(Consensus.PlDeliver.newBuilder()
                                    .setSender(Process.getSelf())
                                    .setMessage(message)
                                    .build())
                            .build()
            );

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(int systemId, ProcessId processTo, Message message) {
        this.displayExecution(systemId, "PlSend", Process.getSelf(), processTo, message);
        Socket socket = null;
        try {
            socket = new Socket(processTo.getHost(), processTo.getPort());

            Utilities.writeMessage(socket.getOutputStream(),
                    Message.newBuilder()
                            .setType(Message.Type.PL_DELIVER)
                            .setSystemId(String.valueOf(systemId))
                            .setPlDeliver(Consensus.PlDeliver.newBuilder()
                                    .setSender(Process.getSelf())
                                    .setMessage(message)
                                    .build())
                            .build()
            );

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

package consensus.eventhandlers;

import consensus.network.process.Process;
import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.net.Socket;

public class PL extends AbstractAlgorithm {

    public PL() {
        this.setName("PL");
    }

    @Override
    public boolean match(Message message) {
        if (message != null && message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case PL_SEND:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean handle(Message message) {
        if (message != null && message.getSystemId().equals(Process.currentSystem)) {
            switch (message.getType()) {
                case PL_SEND:
                    send(message.getSystemId(),
                            message.getAbstractionId(),
                            message.getPlSend().getDestination().getHost(),
                            message.getPlSend().getDestination().getPort(),
                            message.getPlSend().getMessage());
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    private void send(String systemId, String abstractionId, String senderHost, int senderListeningPort, Message message) {
        this.displayExecution(systemId, "PlSend", Process.getSelf(), Utilities.getProcessByAddress(Process.processes, senderHost, senderListeningPort), message);
        Socket socket = null;
        try {
            socket = new Socket(senderHost, senderListeningPort);

            Utilities.writeMessage(socket.getOutputStream(), Message.newBuilder()
                    .setType(Message.Type.NETWORK_MESSAGE)
                    .setSystemId(systemId)
                    .setAbstractionId(abstractionId)
                    .setNetworkMessage(
                            Consensus.NetworkMessage.newBuilder()
                                    .setSenderHost(Process.getSelf().getHost())
                                    .setSenderListeningPort(Process.getSelf().getPort())
                                    .setMessage(message).build())
                    .build());

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

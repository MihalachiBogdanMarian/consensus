package consensus.network.process;

import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Process {

    public static volatile Map<String, ConsensusSystem> systems = new LinkedHashMap<>();
    public static List<ProcessId> processes = new ArrayList<>();
    public static String currentSystem;

    public static String owner;
    public static String address;
    public static int port;

    public final static String HUB_ADDRESS = "127.0.0.1";
    public final static int HUB_PORT = 5000;
    public static volatile Socket hubSocket;
    public static List<Message> appProposes = new LinkedList<>();

    public Process() {
        try {
            hubSocket = new Socket(HUB_ADDRESS, HUB_PORT);
            owner = "bidi";
            address = "127.0.0.1";
            port = Utilities.retrieve("..\\consensus\\src\\main\\resources\\port.txt");
            Utilities.store(port + 1, "..\\consensus\\src\\main\\resources\\port.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Process process = new Process();

        int index = Utilities.retrieve("..\\consensus\\src\\main\\resources\\index.txt");
        // send AppRegistration to the Hub
        System.out.println("Sending AppRegistration...");
        Utilities.writeMessage(hubSocket.getOutputStream(), Message.newBuilder()
                .setType(Message.Type.NETWORK_MESSAGE)
                .setNetworkMessage(
                        Consensus.NetworkMessage.newBuilder()
                                .setSenderHost(address)
                                .setSenderListeningPort(port)
                                .setMessage(Message.newBuilder()
                                        .setAbstractionId("app")
                                        .setType(Message.Type.APP_REGISTRATION)
                                        .setAppRegistration(Consensus.AppRegistration.newBuilder()
                                                .setOwner(owner)
                                                .setIndex(index)
                                                .build())
                                        .build()
                                ).build())
                .build());
        hubSocket.close();
        if (index == 3) {
            Utilities.store(1, "..\\consensus\\src\\main\\resources\\index.txt");
        } else {
            Utilities.store(index + 1, "..\\consensus\\src\\main\\resources\\index.txt");
        }

        System.out.println("Waiting for AppPropose...");

        // listening for messages from other processes
        NetworkListener networkListener = new NetworkListener("NetworkListener", port);
        networkListener.start();

    }

    public static ProcessId getSelf() {
        for (ProcessId process : Process.processes) {
            if (process.getHost().equals(address) && process.getPort() == port) {
                return process;
            }
        }
        return null;
    }

}

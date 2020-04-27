package consensus.network.process;

import consensus.eventhandlers.*;
import consensus.network.server.Hub;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Process {

    public static Map<Integer, ConsensusSystem> systems = new LinkedHashMap<>();
    public static List<ProcessId> processes = new ArrayList<>();

    public static String owner;
    public static String address;
    public static int port;
    public static List<String> fileNames = new ArrayList<>();

    public final static String HUB_ADDRESS = "127.0.0.1";
    public final static int HUB_PORT = 8100;
    public static volatile Socket hubSocket;

    public Process() {
        try {
            hubSocket = new Socket(HUB_ADDRESS, HUB_PORT);
            owner = "Bidi";
            address = "127.0.0.1";
            port = Utilities.retrieve("..\\consensus\\src\\main\\resources\\port.txt");
            Utilities.store(port + 1, "..\\consensus\\src\\main\\resources\\port.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Process process = new Process();
        System.out.println("Waiting for AppPropose...");

        // send AppRegistration to Hub
        Utilities.writeMessage(hubSocket.getOutputStream(), Message.newBuilder()
                .setType(Message.Type.APP_REGISTRATION)
                .setAppRegistration(
                        Consensus.AppRegistration.newBuilder()
                                .setOwner(owner)
                                .setIndex(port % 10)
                                .setPort(port)
                                .build()
                ).build());

        // read AppProposes
        List<Message> appProposes = new LinkedList<>();
        for (int i = 1; i <= Hub.nrSystems; i++) {
            appProposes.add(Utilities.readMessage(hubSocket.getInputStream()));
        }
//        for (int i = 1; i <= Hub.nrSystems; i++) {
//            System.out.println(appProposes.get(i - 1));
//        }
        hubSocket.close();

        // fill the systems
        for (Message appPropose : appProposes) {
            processes = appPropose.getAppPropose().getProcessesList();
            Map<String, AbstractAlgorithm> algorithms = new HashMap<>();
            algorithms.put("UC", new UC());
            algorithms.put("EC", new EC());
            algorithms.put("OMEGA", new OMEGA());
            algorithms.put("BEB", new BEB());
            algorithms.put("PL", new PL());
            systems.put(Integer.parseInt(appPropose.getSystemId()),
                    new ConsensusSystem(appPropose.getAppPropose().getValue(), processes, algorithms));
        }
        System.out.println(systems);
        System.out.println(processes);

        List<ProcessId> processes = appProposes.get(0).getAppPropose().getProcessesList();
        for (Map.Entry<Integer, ConsensusSystem> entry : systems.entrySet()) {
            fileNames.add("..\\consensus\\src\\main\\resources\\recovery" + entry.getKey() + "." + Utilities.rank(processes, getSelf()) + ".txt");
        }

        // listening for messages from other processes
        NetworkListener networkListener = new NetworkListener("NetworkListener", port);
        networkListener.start();

        // Events Queue - listening for events <-> messages and handling them in order
        EventsThread eventsThread = new EventsThread("EventsThread");
        eventsThread.start();

        // start the algorithms
        for (Map.Entry<Integer, ConsensusSystem> entry : systems.entrySet()) {
            entry.getValue().algorithms.get("OMEGA").init(entry.getKey());
            entry.getValue().algorithms.get("EC").init(entry.getKey());
            entry.getValue().algorithms.get("UC").init(entry.getKey());
            entry.getValue().eventsQueue.insert(Message.newBuilder()
                    .setSystemId(String.valueOf(entry.getKey()))
                    .setType(Message.Type.UC_PROPOSE)
                    .setUcPropose(Consensus.UcPropose.newBuilder()
                            .setValue(entry.getValue().valueToPropose)
                            .build())
                    .build());
        }
    }

    public static ProcessId getSelf() {
        for (ProcessId process : Process.processes) {
            if (process.getPort() == port) {
                return process;
            }
        }
        return null;
    }
}

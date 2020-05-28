package consensus.network.process;

import consensus.eventhandlers.*;
import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NetworkListener extends Thread {

    private Thread t;
    private String threadName;
    private static int port;
    private ServerSocket processSocket;
    private boolean running;

    public NetworkListener(String threadName, int port) {
        this.threadName = threadName;
        this.port = port;
        try {
            setProcessSocket(new ServerSocket(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
    }

    public void run() {
        Socket socket = null;
        try {
            while (running) {
                socket = getProcessSocket().accept();

                // read message from hub/process
                Message message = Utilities.readMessage(socket.getInputStream());

                if (message.getNetworkMessage().getMessage().getType().equals(Message.Type.APP_PROPOSE)) {
                    // received AppPropose from the Hub
                    Process.appProposes.add(message.getNetworkMessage().getMessage());

                    // fill the systems
                    Message appPropose = Process.appProposes.get(Process.appProposes.size() - 1);
                    Process.processes = appPropose.getAppPropose().getProcessesList();
                    Map<String, AbstractAlgorithm> algorithms = new HashMap<>();
                    algorithms.put("UC", new UC());
                    algorithms.put("EC", new EC());
                    algorithms.put("ELD", new ELD());
                    algorithms.put("EPFD", new EPFD());
                    algorithms.put("BEB", new BEB());
                    algorithms.put("PL", new PL());
                    Process.systems.put(message.getSystemId(),
                            new ConsensusSystem(appPropose.getAppPropose().getValue().getV(), Process.processes, algorithms));

                    System.out.println(Process.systems.get(message.getSystemId()));

                    // start the algorithms
                    ConsensusSystem consensusSystem = Process.systems.get(message.getSystemId());
                    consensusSystem.algorithms.get("EPFD").init(message.getSystemId());
                    consensusSystem.algorithms.get("ELD").init(message.getSystemId());
                    consensusSystem.algorithms.get("EC").init(message.getSystemId());
                    consensusSystem.algorithms.get("UC").init(message.getSystemId());
                    consensusSystem.eventsQueue.add(Message.newBuilder()
                            .setSystemId(String.valueOf(message.getSystemId()))
                            .setAbstractionId("uc")
                            .setType(Message.Type.UC_PROPOSE)
                            .setUcPropose(Consensus.UcPropose.newBuilder()
                                    .setValue(Consensus.Value.newBuilder()
                                            .setDefined(true)
                                            .setV(consensusSystem.valueToPropose)
                                            .build())
                                    .build())
                            .build());

                    // Events Queue - listening for events <-> messages and handling them in order
                    EventsThread eventsThread = new EventsThread("EventsThread", message.getSystemId());
                    eventsThread.start();

                } else {
                    // received a message from a process
                    Process.systems.get(message.getSystemId()).eventsQueue.add(
                            Message.newBuilder().setType(Message.Type.PL_DELIVER)
                                    .setSystemId(message.getSystemId())
                                    .setAbstractionId(message.getAbstractionId())
                                    .setPlDeliver(Consensus.PlDeliver.newBuilder()
                                            .setSender(Utilities.getProcessByAddress(Process.processes, message.getNetworkMessage().getSenderHost(), message.getNetworkMessage().getSenderListeningPort()))
                                            .setMessage(message.getNetworkMessage().getMessage())
                                            .build())
                                    .build()
                    );
                }
//                System.out.println(message);

                socket.close();
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public ServerSocket getProcessSocket() {
        return processSocket;
    }

    public void setProcessSocket(ServerSocket processSocket) {
        this.processSocket = processSocket;
    }
}

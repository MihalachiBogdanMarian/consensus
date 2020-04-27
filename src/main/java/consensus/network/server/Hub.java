package consensus.network.server;

import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Hub {

    protected static Map<ProcessId, OutputStream> processes = new HashMap<>(); // the process we work with <-> process output stream

    private static final int PORT = 8100;
    private ServerSocket hubSocket;

    private boolean running = false;
    private int nrProcesses = 0;

    protected static final int finalNrProcesses = 3;
    int[] ranks = new int[finalNrProcesses];

    public static final int nrSystems = 1;

    public static void main(String[] args) throws IOException {
        Hub hub = new Hub();
        hub.init();
        hub.waitForProcesses();
    }

    public void init() throws IOException {
        setHubSocket(new ServerSocket(PORT));
        running = true;
        for (int i = 0; i < finalNrProcesses; i++) {
            ranks[i] = 0;
        }
    }

    public void waitForProcesses() throws IOException {
        try {
            while (running) {
                System.out.println("Waiting for a process...");
                Socket socket = getHubSocket().accept();
                System.out.println("A process is connected...");

                // read AppRegistration/AppDecide from process
                Message message = Utilities.readMessage(socket.getInputStream());
                switch (message.getType()) {
                    case APP_REGISTRATION:
                        System.out.println(message);

                        Random rand = new Random();
                        int rank = rand.nextInt(finalNrProcesses) + 1;
                        while (ranks[rank - 1] == 1) {
                            rank = rand.nextInt(finalNrProcesses) + 1;
                        }
                        ranks[rank - 1] = 1;

                        processes.put(ProcessId.newBuilder()
                                        .setHost("127.0.0.1")
                                        .setPort(message.getAppRegistration().getPort())
                                        .setOwner(message.getAppRegistration().getOwner())
                                        .setIndex(message.getAppRegistration().getIndex())
                                        .setRank(rank)
                                        .build(),
                                new DataOutputStream(socket.getOutputStream())
                        );

                        // new thread to attend to the process requests
                        new ProcessThread(this, this.getHubSocket(), socket).start();
                        break;
                    case APP_DECIDE:
                        System.out.println("PROCESS (" + message.getAppDecide().getSender().getOwner() + ", " +
                                message.getAppDecide().getSender().getHost() + ", " + message.getAppDecide().getSender().getPort() +
                                "), for SYSTEM " + message.getSystemId() + ", has decided value --> " + message.getAppDecide().getValue());
                        socket.close();
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Ooops... " + e);
        } finally {
            getHubSocket().close();
        }
    }

    public void stop() throws IOException {
        this.running = false;
        getHubSocket().close();
    }

    public ServerSocket getHubSocket() {
        return hubSocket;
    }

    public void setHubSocket(ServerSocket hubSocket) {
        this.hubSocket = hubSocket;
    }

    public int getNrProcesses() {
        return nrProcesses;
    }

    public void setNrProcesses(int nrProcesses) {
        this.nrProcesses = nrProcesses;
    }
}

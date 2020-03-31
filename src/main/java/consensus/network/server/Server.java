package consensus.network.server;

import consensus.protos.Consensus.ProcessId;
import consensus.utilities.Utilities;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server {

    protected static Map<ProcessId, OutputStream> processes = new HashMap<>(); // process id <-> process output stream
    protected static int nrValuesDecided = 0;
    protected static boolean endSent = false;

    private static final int PORT = 8100;
    private ServerSocket serverSocket;
    private boolean running = false;
    private int nrProcesses = 0;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.init();
        server.waitForProcesses();
    }

    public void init() throws IOException {
        setServerSocket(new ServerSocket(PORT));
        running = true;
    }

    public void waitForProcesses() throws IOException {
        try {
            while (running) {

                System.out.println("Waiting for a process...");
                Socket socket = getServerSocket().accept();
                System.out.println("A process is connected...");
                this.setNrProcesses(this.getNrProcesses() + 1);

                Random rand = new Random();
                processes.put(
                        ProcessId.newBuilder().setHost(socket.getLocalAddress().toString().split("/")[1]).setPort(socket.getPort()).setOwner("Bogdan").setIndex(rand.nextInt(1000) + 1).build(),
                        new DataOutputStream(socket.getOutputStream())
                );

                // send each process his corresponding port
                OutputStream out = socket.getOutputStream();
                out.write(Utilities.intToBytes(socket.getPort()), 0, Integer.SIZE / Byte.SIZE);

                // new thread to attend to the process requests
                new ProcessThread(this, this.getServerSocket(), socket).start();
            }
        } catch (IOException e) {
            System.err.println("Ooops... " + e);
        } finally {
            getServerSocket().close();
        }
    }

    public void stop() throws IOException {
        this.running = false;
        getServerSocket().close();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public int getNrProcesses() {
        return nrProcesses;
    }

    public void setNrProcesses(int nrProcesses) {
        this.nrProcesses = nrProcesses;
    }
}

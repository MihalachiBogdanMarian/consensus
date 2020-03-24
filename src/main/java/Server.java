import com.example.tutorial.Consensus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static List<Consensus.ProcessId> processes = new ArrayList<>();

    private static final int PORT = 8100;
    private ServerSocket serverSocket;
    private boolean running = false;
    private int nrClients = 0;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.init();
        server.waitForClients();
    }

    public void init() throws IOException {
        setServerSocket(new ServerSocket(PORT));
        running = true;
    }

    public void waitForClients() throws IOException {
        try {
            while (running) {
                System.out.println("Waiting for a client ...");
                Socket socket = getServerSocket().accept();
                System.out.println("A client has arrived...");
                this.setNrClients(this.getNrClients() + 1);
                processes.add(Consensus.ProcessId.newBuilder().setHost(socket.getLocalAddress().toString().split("/")[1]).setPort(socket.getPort()).setOwner("Bogdan").setIndex(this.getNrClients()).build());
                new ClientThread(this, this.getServerSocket(), socket).start();
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

    public int getNrClients() {
        return nrClients;
    }

    public void setNrClients(int nrClients) {
        this.nrClients = nrClients;
    }

    public static List<Consensus.ProcessId> getProcesses() {
        return processes;
    }
}

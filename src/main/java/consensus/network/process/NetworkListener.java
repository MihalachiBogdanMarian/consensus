package consensus.network.process;

import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkListener extends Thread {

    private Thread t;
    private String threadName;
    private static int port;
    private ServerSocket processSocket;
    private boolean running = false;

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

                // read AppRegistration/AppDecide from process
                Message message = Utilities.readMessage(socket.getInputStream());
                Process.systems.get(message.getSystemId()).eventsQueue.insert(message);
//                System.out.println(message);

                socket.close();
            }
        } catch (IOException e) {
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

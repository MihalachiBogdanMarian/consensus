package consensus.network;

import consensus.protos.Consensus.ProcessId;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {

    private Socket socket;
    private final Server server;
    private List<OutputStream> clients;

    public ClientThread(Server server, ServerSocket serverSocket, Socket clientSocket, List<OutputStream> clients) {
        this.server = server;
        this.server.setServerSocket(serverSocket);
        this.socket = clientSocket;
        this.clients = clients;
    }

    @Override
    public void run() {

        BufferedReader in = null;
        try {
            sendValueToProposeAndProcesses();

            // waiting for client's request
            System.out.println("Waiting for client's request...");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            System.out.println(response);

            while (true) {
                // waiting for client's request
                System.out.println("Waiting for client's request...");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                response = in.readLine();
                System.out.println(response);
                if (response.equals("stop")) {
                    break;
                }
            }
            if (server.getNrClients() > 0) {
                server.setNrClients(server.getNrClients() - 1);
                socket.close();
            }
            if (server.getNrClients() == 0) {
                socket.close();
                server.stop();
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                assert in != null;
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected static byte[] intToBytes(final int data) {
        return new byte[]{
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) (data & 0xff),
        };
    }

    private void sendValueToProposeAndProcesses() {
        try {
            OutputStream out = socket.getOutputStream();

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter value to be proposed: ");
            int value = Integer.parseInt(stdin.readLine());
            out.write(intToBytes(value), 0, Integer.SIZE / Byte.SIZE);

            out.write(intToBytes(Server.getProcesses().size()), 0, Integer.SIZE / Byte.SIZE);
            for (ProcessId processId : Server.getProcesses()) {
                byte[] processIdBytes = processId.toByteArray();
                out.write(intToBytes(processIdBytes.length), 0, Integer.SIZE / Byte.SIZE);
                out.write(processIdBytes, 0, processIdBytes.length);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
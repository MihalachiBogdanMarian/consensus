package consensus.network.server;

import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;
import consensus.utilities.Utilities;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessThread extends Thread {

    private Socket socket;
    private final Server server;

    public ProcessThread(Server server, ServerSocket serverSocket, Socket processSocket) {
        this.server = server;
        this.server.setServerSocket(serverSocket);
        this.socket = processSocket;
    }

    @Override
    public void run() {

        InputStream in = null;
        try {
            sendValueToProposeAndProcesses();

            while (true) {
                in = socket.getInputStream();

                ProcessId processFrom = Utilities.readProcess(in);
                ProcessId processTo = Utilities.readProcess(in);
                Message message = Utilities.readMessage(in);

                if (message.getType().equals(Message.Type.UC_DECIDE)) {
                    System.out.println("Process (" + processFrom.getHost() + ", " + processFrom.getPort() + ", " + processFrom.getOwner() + ", " + processFrom.getIndex() + ") has proposed " + message.getUcDecide().getValue());
                    break;
                }

                synchronized (Server.processes) {
                    sendMessageToProcess(processFrom, processTo, message);
                }
            }
            if (server.getNrProcesses() > 0) {
                server.setNrProcesses(server.getNrProcesses() - 1);
                socket.close();
            }
            if (server.getNrProcesses() == 0) {
                socket.close();
                server.stop();
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                assert in != null;
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ProcessThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sendValueToProposeAndProcesses() {
        try {
            OutputStream out = socket.getOutputStream();

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter value to be proposed: ");
            Utilities.writeMessage(out,
                    Message.newBuilder().setType(Message.Type.UC_PROPOSE)
                            .setUcPropose(Consensus.UcPropose.newBuilder().
                                    setValue(Integer.parseInt(stdin.readLine())).build()).build());

            out.write(Utilities.intToBytes(Server.processes.size()), 0, Integer.SIZE / Byte.SIZE);
            for (ProcessId processId : Server.processes.keySet()) {
                Utilities.writeProcess(out, processId);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToProcess(ProcessId processFrom, ProcessId processTo, Message message) {
        try {
            OutputStream out = Server.processes.get(processTo);

            Utilities.writeProcess(out, processFrom);
            Utilities.writeMessage(out, message);

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
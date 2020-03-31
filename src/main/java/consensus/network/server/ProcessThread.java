package consensus.network.server;

import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;
import consensus.utilities.Utilities;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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

            in = socket.getInputStream();
            while (true) {

                ProcessId processFrom = Utilities.readProcess(in);
                ProcessId processTo = Utilities.readProcess(in);

                Message message = Utilities.readMessage(in);

//                System.out.println("Sending (From: " + processFrom.toString().replace("\n", " ") + ", To: " + processTo.toString().replace("\n", " ") + ", Message: " + message.toString().replace("\n", " ") + ") ...");

                if (message.getType().equals(Message.Type.UC_DECIDE)) {
                    System.out.println("Process (" + processFrom.getHost() + ", " + processFrom.getPort() + ", " + processFrom.getOwner() + ", " + processFrom.getIndex() +
                            ") has decided value: " + message.getUcDecide().getValue());
//                    Server.nrValuesDecided++;
//                    if (Server.nrValuesDecided == Server.processes.size()) {
//                        if (!Server.endSent) {
//                            for (Map.Entry<ProcessId, OutputStream> entry : Server.processes.entrySet()) {
//                                OutputStream out = entry.getValue();
//                                Utilities.writeProcess(out, entry.getKey());
//                                Utilities.writeMessage(out, Message.newBuilder().setType(Message.Type.END).setEnd(Consensus.End.newBuilder().build()).build());
//                                out.flush();
//                            }
//                            Server.endSent = true;
//                        }
//                        break;
//                    }
                } else {
//                    if (Server.nrValuesDecided == 0) {
                    synchronized (Server.processes) {
                        sendMessageToProcess(processFrom, processTo, message);
//                        }
                    }
                }

//                if (Server.nrValuesDecided == Server.processes.size()) {
//                    break;
//                }

                if (false) {
                    break;
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
            int value = Integer.parseInt(stdin.readLine());
            Utilities.writeMessage(out,
                    Message.newBuilder().setType(Message.Type.UC_PROPOSE)
                            .setUcPropose(Consensus.UcPropose.newBuilder().
                                    setValue(value).build()).build());

            out.write(Utilities.intToBytes(Server.processes.size()), 0, Integer.SIZE / Byte.SIZE);

            Server.processes = indexing(Server.processes);

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

    private Map<ProcessId, OutputStream> indexing(Map<ProcessId, OutputStream> processes) {
        Map<ProcessId, OutputStream> processesIndexed = new HashMap<>();

        List<ProcessId> processList = new ArrayList<>(processes.keySet());
        processList.sort((p1, p2) -> {
            if (p2.getIndex() < p1.getIndex()) {
                return 1;
            } else if (p2.getIndex() > p1.getIndex()) {
                return -1;
            } else {
                return 0;
            }
        });

        for (Map.Entry<ProcessId, OutputStream> entry : processes.entrySet()) {
            ProcessId key = entry.getKey();
            OutputStream out = entry.getValue();
            processesIndexed.put(ProcessId.newBuilder().setHost(key.getHost()).setPort(key.getPort()).setOwner(key.getOwner()).setIndex(index(processList, key)).build(), out);
        }

        return processesIndexed;
    }

    private int index(List<ProcessId> processList, ProcessId process) {
        for (int i = 0; i < processList.size(); i++) {
            if (processList.get(i).equals(process)) {
                return i + 1;
            }
        }
        return 0;
    }

}
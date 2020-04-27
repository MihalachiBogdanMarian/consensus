package consensus.network.server;

import consensus.protos.Consensus;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ProcessThread extends Thread {

    private Socket socket;
    private final Hub hub;

    public ProcessThread(Hub hub, ServerSocket hubSocket, Socket processSocket) {
        this.hub = hub;
        this.hub.setHubSocket(hubSocket);
        this.socket = processSocket;
    }

    @Override
    public void run() {
//        while (true) {
//            if (hub.getNrProcesses() == Hub.finalNrProcesses) {
        for (int i = 1; i <= Hub.nrSystems; i++) {
            sendAppPropose(i);
        }
        Utilities.store(8101, "..\\consensus\\src\\main\\resources\\port.txt");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//                break;
//            }
//        }
    }

    private void sendAppPropose(int systemId) {
        try {
            OutputStream out = socket.getOutputStream();

//            Random rand = new Random();
//            int value = rand.nextInt(1000) + 1;
//            System.out.print("Value to be proposed: " + value);
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter value to be proposed: ");
            int value = Integer.parseInt(stdin.readLine());

            Message appPropose = Message.newBuilder()
                    .setSystemId(String.valueOf(systemId))
                    .setAppPropose(Consensus.AppPropose.newBuilder()
                            .setValue(value)
                            .addAllProcesses(new ArrayList<>(Hub.processes.keySet()))
                            .build())
                    .build();

            Utilities.writeMessage(out, appPropose);

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
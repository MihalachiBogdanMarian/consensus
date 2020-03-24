import com.example.tutorial.AddressBookProtos;
import com.example.tutorial.AddressBookProtos.Person;
import com.example.tutorial.Consensus.ProcessId;
import consensus.eventhandlers.AbstractEvent;
import consensus.eventsqueue.Queue;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private static Queue<AbstractEvent> eventsQueue = new Queue<>();
    private static List<ProcessId> processes = new ArrayList<>();
    private static int valueToPropose;

    private final static String SERVER_ADDRESS = "127.0.0.1";
    private final static int PORT = 8100;

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        Socket socket = new Socket(SERVER_ADDRESS, PORT);

        System.out.println("Waiting from message from server... ");

        readValueToProposeAndProcesses(socket);
        System.out.println(valueToPropose);
        for (ProcessId processId : processes) {
            System.out.println(processId.toString());
        }

        while (true) {
            // send response to server based on the message received
            String response = client.readFromKeyboard(); // ok, nok or stop
            if (response.equalsIgnoreCase("exit")) {
                socket.close();
                break;
            } else {
                client.sendResponseToServer(response, socket);
            }
        }
    }

    public void sendResponseToServer(String request, Socket socket) throws IOException {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(request);
        } catch (UnknownHostException e) {
            System.err.println("No server listening... " + e);
        }
    }

    private String readFromKeyboard() {
        System.out.print("Say something to the server: ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static int bytesToInt(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        return ((0xff & data[0]) << 24 |
                (0xff & data[1]) << 16 |
                (0xff & data[2]) << 8 |
                (0xff & data[3])
        );
    }

    private static void readValueToProposeAndProcesses(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            byte[] valueBytes = new byte[Integer.SIZE / Byte.SIZE];
            in.read(valueBytes, 0, Integer.SIZE / Byte.SIZE);
            valueToPropose = bytesToInt(valueBytes);

            byte[] processesLengthBytes = new byte[Integer.SIZE / Byte.SIZE];
            in.read(processesLengthBytes, 0, Integer.SIZE / Byte.SIZE);
            for (int i = 0; i < bytesToInt(processesLengthBytes); i++) {
                byte[] lengthBytes = new byte[Integer.SIZE / Byte.SIZE];
                in.read(lengthBytes, 0, Integer.SIZE / Byte.SIZE);
                byte[] processIdBytes = new byte[bytesToInt(lengthBytes)];
                in.read(processIdBytes, 0, bytesToInt(lengthBytes));
                processes.add(ProcessId.parseFrom(processIdBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

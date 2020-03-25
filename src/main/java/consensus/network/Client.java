package consensus.network;

import consensus.eventhandlers.Timeout;
import consensus.protos.Consensus.ProcessId;
import consensus.eventhandlers.AbstractEvent;
import consensus.eventsqueue.Queue;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class Client {

    public static Queue<AbstractEvent> eventsQueue = new Queue<>();
    public static List<ProcessId> processes = new ArrayList<>();
    public static int port;
    // UC
    public static int val;
    public static boolean proposed;
    public static boolean decided;
    public static int ets; // current timestamp (of the current epoch)
    public static ProcessId l; // the current leader (of the current epoch)
    public static int newts; // the new epoch
    public static ProcessId newl; // the new leader
    public static ProcessId l0; // the initial leader
    // EC
    public static ProcessId trusted;
    public static int lastts; // last epoch that it started
    public static int ts; // timestamp of an epoch at which it tried to be leader
    // EP
    // ELD
    public static int epoch; // current epoch
    public static List<SimpleEntry<ProcessId, Integer>> candidates;
    public static int delay;
    public final static int delta = 10;
    // BEB
    // PL


    private final static String SERVER_ADDRESS = "127.0.0.1";
    private final static int PORT = 8100;

    public static void main(String[] args) throws IOException {
        System.out.println(retrieve());
        Client client = new Client();
        Socket socket = new Socket(SERVER_ADDRESS, PORT);

        // read my port
        InputStream in = socket.getInputStream();
        byte[] portBytes = new byte[Integer.SIZE / Byte.SIZE];
        in.read(portBytes, 0, Integer.SIZE / Byte.SIZE);
        port = bytesToInt(portBytes);
        System.out.println(port);

        // Events Queue
        EventsThread eventsThread = new EventsThread("EventsThread");
        eventsThread.start();

        // Reading the value to propose and all available processes
        System.out.println("Waiting from message from server... ");

        readValueToProposeAndProcesses(socket);
        System.out.println(val);
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
            val = bytesToInt(valueBytes);

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

    public static void store(int epoch) {
        try {
            FileWriter writer = new FileWriter("C:\\Users\\BiDi\\Documents\\IntelliJProjects\\consensus\\src\\main\\resources\\epoch.txt", false);
            writer.write(String.valueOf(epoch));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int retrieve() {
        int epoch = -1;
        try {
            FileReader reader = new FileReader("C:\\Users\\BiDi\\Documents\\IntelliJProjects\\consensus\\src\\main\\resources\\epoch.txt");
            BufferedReader bufferedReader = new BufferedReader(reader);

            epoch = Integer.parseInt(bufferedReader.readLine());

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return epoch;
    }

    public static void starttimer(int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Client.eventsQueue.insert(new Timeout());
            }
        }, delay * 1000);
    }

}

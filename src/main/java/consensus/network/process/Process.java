package consensus.network.process;

import consensus.eventhandlers.*;
import consensus.eventsqueue.Queue;
import consensus.protos.Consensus.ProcessId;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class Process {

    public static volatile Queue<AbstractEvent> eventsQueue = new Queue<>();
    public static List<ProcessId> processes = new LinkedList<>();
    public static int port;
    public static String fileName = "";
    public static boolean runForever = true;
    // UC
    public static Integer val;
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
    public static Map<Integer, EpInstance> epInstances = new HashMap<>();
    // ELD
    public static int epoch; // how many times the process crashed and recovered
    public static LinkedList<SimpleEntry<ProcessId, Integer>> candidates;
    public static int delay;
    public final static int delta = 1000; // milliseconds


    private final static String SERVER_ADDRESS = "127.0.0.1";
    private final static int PORT = 8100;
    public static volatile Socket socket;

    public Process() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Process process = new Process();

        // read my port
        InputStream in = socket.getInputStream();
        byte[] portBytes = new byte[Integer.SIZE / Byte.SIZE];
        in.read(portBytes, 0, Integer.SIZE / Byte.SIZE);
        port = Utilities.bytesToInt(portBytes);
//        System.out.println(port);

        // Events Queue - listening for events and handling them in order
        EventsThread eventsThread = new EventsThread("EventsThread");
        eventsThread.start();

        System.out.println("Waiting from message from server... ");
        // the value I have to propose and all the processes I work with
        int v = readValueToProposeAndProcesses(socket);
        fileName = "C:\\Users\\BiDi\\Documents\\IntelliJProjects\\consensus\\src\\main\\resources\\rank" + Utilities.rank(Process.processes, Process.getSelf()) + ".txt";
        l0 = Utilities.maxrank(processes);
        l = l0;

        System.out.println(v);
        System.out.println();
        for (ProcessId processId : processes) {
            System.out.println(processId.toString());
        }
        System.out.println();

        // listening for messages from other processes
        PlDeliver plDeliver = new PlDeliver("PlDeliver");
        plDeliver.start();

        // start the algorithms
        eventsQueue.insert(new EcInit());
        eventsQueue.insert(new OmegaInit());
        eventsQueue.insert(new UcInit());
        eventsQueue.insert(new UcPropose(v));

        while (true) {
            if (!runForever) {
                break;
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

    private static int readValueToProposeAndProcesses(Socket socket) {
        int v = 0;
        try {
            InputStream in = socket.getInputStream();

            v = Utilities.readMessage(in).getUcPropose().getValue();

            byte[] processesLengthBytes = new byte[Integer.SIZE / Byte.SIZE];
            in.read(processesLengthBytes, 0, Integer.SIZE / Byte.SIZE);
            for (int i = 0; i < Utilities.bytesToInt(processesLengthBytes); i++) {
                processes.add(Utilities.readProcess(in));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;
    }

    public static ProcessId getSelf() {
        for (ProcessId process : processes) {
            if (process.getPort() == port) {
                return process;
            }
        }
        return null;
    }

}

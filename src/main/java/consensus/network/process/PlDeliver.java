package consensus.network.process;

import consensus.eventhandlers.BebDeliver;
import consensus.eventhandlers.OmegaRecovery;
import consensus.network.server.ProcessThread;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlDeliver extends Thread {
    private Thread t;
    private String threadName;
    private static boolean crash = false;
    private static boolean hasCrashed = false;

    public PlDeliver(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        InputStream in = null;
        try {
            in = Process.socket.getInputStream();

            while (true) {

//                if (Process.l0 != null && Process.l0.equals(Process.getSelf()) && !hasCrashed) {
//                    hasCrashed = true;
//                    starttimer(100);
//                }

                // we received from process processFrom message Message
                ProcessId processFrom = Utilities.readProcess(in);
                Message message = Utilities.readMessage(in);

                this.displayExecution(processFrom, message);

                switch (message.getType()) {
                    case ELD_HEARTBEAT_:
                        int exists = Utilities.exists(Process.candidates, processFrom, message.getEldHeartbeat().getEpoch());
                        if (exists != -1) {
                            Process.candidates.remove(new SimpleEntry<>(processFrom, exists));
                        }
                        Utilities.addInOrder(Process.processes, Process.candidates, processFrom, message.getEldHeartbeat().getEpoch());
                        break;
                    case BEB_BROADCAST:
                        Process.eventsQueue.insert(
                                new BebDeliver(
                                        processFrom, message.getBebBroadcast().getMessage()
                                )
                        );
                        break;
                    case EC_NACK_:
                        if (Process.trusted.equals(Process.getSelf())) {
                            Process.ts += Process.processes.size();
                            Process.eventsQueue.insert(new consensus.eventhandlers.BebBroadcast(
                                    Message.newBuilder().setType(Message.Type.BEB_BROADCAST)
                                            .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                            .setType(Message.Type.EC_NEW_EPOCH_)
                                                            .setEcNewEpoch(Consensus.EcNewEpoch_.newBuilder().setTimestamp(Process.ts).build())
                                                            .build()
                                                    ).build()
                                            ).build()
                            ));
                        }
                        break;
                    case EP_STATE_:
                        if (!Process.epInstances.get(message.getEpState().getEpTimestamp()).isAborted()) {
                            Process.epInstances.get(message.getEpState().getEpTimestamp()).getStates().put(processFrom,
                                    new EpState(message.getEpState().getValueTimestamp(), message.getEpState().getValue()));
                            if (Utilities.hashtag(Process.epInstances.get(message.getEpState().getEpTimestamp()).getStates()) > Process.processes.size() / 2) {
                                EpState epState = Utilities.highest(Process.epInstances.get(message.getEpState().getEpTimestamp()).getStates());

                                if (epState.getValue() != 0) {
                                    Process.epInstances.get(message.getEpState().getEpTimestamp()).setTmpval(epState.getValue());
                                }

                                Map<ProcessId, EpState> epStates = new HashMap<>();
                                for (int i = 0; i < Process.processes.size(); i++) {
                                    epStates.put(Process.processes.get(i), null);
                                }
                                Process.epInstances.get(message.getEpState().getEpTimestamp()).setStates(epStates);

                                Process.eventsQueue.insert(new consensus.eventhandlers.BebBroadcast(
                                        Message.newBuilder().setType(Message.Type.BEB_BROADCAST)
                                                .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                                .setType(Message.Type.EP_WRITE_)
                                                                .setEpWrite(Consensus.EpWrite_.newBuilder()
                                                                        .setValue(Process.epInstances.get(message.getEpState().getEpTimestamp()).getTmpval())
                                                                        .setEpTimestamp(message.getEpState().getEpTimestamp()).build())
                                                                .build()
                                                        ).build()
                                                ).build()
                                ));
                            }
                        }
                        break;
                    case EP_ACCEPT_:
                        if (!Process.epInstances.get(message.getEpAccept().getEpTimestamp()).isAborted()) {
                            Process.epInstances.get(message.getEpAccept().getEpTimestamp()).setAccepted(Process.epInstances.get(message.getEpAccept().getEpTimestamp()).getAccepted() + 1);
                            if (Process.epInstances.get(message.getEpAccept().getEpTimestamp()).getAccepted() > Process.processes.size() / 2) {
                                Process.epInstances.get(message.getEpAccept().getEpTimestamp()).setAccepted(0);

                                Process.eventsQueue.insert(new consensus.eventhandlers.BebBroadcast(
                                        Message.newBuilder().setType(Message.Type.BEB_BROADCAST)
                                                .setBebBroadcast(Consensus.BebBroadcast.newBuilder().setMessage(Message.newBuilder()
                                                                .setType(Message.Type.EP_DECIDED_)
                                                                .setEpDecided(Consensus.EpDecided_.newBuilder()
                                                                        .setValue(Process.epInstances.get(message.getEpAccept().getEpTimestamp()).getTmpval())
                                                                        .setEpTimestamp(message.getEpAccept().getEpTimestamp()).build())
                                                                .build()
                                                        ).build()
                                                ).build()
                                ));
                            }
                        }
                        break;
                    case END:
                        System.out.println("Goodbye!");
                        Process.runForever = false;
                        break;
                    default:
                        break;
                }

                if (crash) {
                    simulateCrachRecovery(5);
                }

//                if (!Process.runForever) {
//                    break;
//                }
            }
        } catch (
                IOException ex) {
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

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public void displayExecution(ProcessId processFrom, Message message) {
        synchronized (System.out) {
            System.out.println("PlDeliver (From: " + processFrom.getIndex()
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }

    public static void simulateCrachRecovery(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Process.eventsQueue.insert(new OmegaRecovery());
        crash = false;
    }

    private static void starttimer(int delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                crash = true;
            }
        }, delay);
    }
}

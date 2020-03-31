package consensus.network.process;

import consensus.eventhandlers.BebDeliver;
import consensus.network.server.ProcessThread;
import consensus.protos.Consensus;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;
import consensus.utilities.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlDeliver extends Thread {
    private Thread t;
    private String threadName;

    public PlDeliver(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        InputStream in = null;
        try {
            in = Process.socket.getInputStream();
            while (true) {
                // we received from process processFrom message Message
                ProcessId processFrom = Utilities.readProcess(in);
                Message message = Utilities.readMessage(in);

//                this.displayExecution(processFrom, message);

                switch (message.getType()) {
                    case ELD_HEARTBEAT_:
                        if (Utilities.exists(Process.candidates, processFrom, message.getEldHeartbeat().getEpoch())) {
                            Process.candidates.remove(new SimpleEntry<>(processFrom, message.getEldHeartbeat().getEpoch()));
                        }
                        Utilities.addInOrder(Process.candidates, processFrom, message.getEldHeartbeat().getEpoch());
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
                        Process.epInstances.get(message.getEpState().getEpTimestamp()).getStates().put(processFrom,
                                new EpState(message.getEpState().getValueTimestamp(), message.getEpState().getValue()));
                        if (Utilities.hashtag(Process.epInstances.get(message.getEpState().getEpTimestamp()).getStates()) > Process.processes.size() / 2) {
                            EpState epState = Utilities.highest(Process.epInstances.get(message.getEpState().getEpTimestamp()).getStates());
                            if (epState.getValue() != 0) {
                                Process.epInstances.get(message.getEpState().getEpTimestamp()).setTmpval(epState.getValue());
                            }
                            for (int i = 0; i < Process.processes.size(); i++) {
                                Process.epInstances.get(message.getEpState().getEpTimestamp()).getStates().put(Process.processes.get(i), null);
                            }

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
                        break;
                    case EP_ACCEPT_:
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
                        break;
                    case END:
                        System.out.println("Goodbye!");
                        Process.runForever = false;
                        break;
                    default:
                        break;
                }

                if (!Process.runForever) {
                    break;
                }
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
            System.out.println("PlDeliver (From: " + processFrom.toString().replace("\n", " ")
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }
}

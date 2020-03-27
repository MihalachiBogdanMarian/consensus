package consensus.network.process;

import consensus.eventhandlers.BebBroadcast;
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

public class PlDeliver implements Runnable {
    private Thread t;
    private String threadName;

    public PlDeliver(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        InputStream in = null;
        try {
            while (true) {
                in = Process.socket.getInputStream();

                // we received from process processFrom message Message
                ProcessId processFrom = Utilities.readProcess(in);
                Message message = Utilities.readMessage(in);

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
                                    Message.newBuilder().setType(Message.Type.BEB_BROADCAST).
                                            setBebBroadcast(
                                                    Consensus.BebBroadcast.newBuilder().setMessage(
                                                            Message.newBuilder().setType(Message.Type.EC_NEW_EPOCH_).
                                                                    setEcNewEpoch(Consensus.EcNewEpoch_.newBuilder().setTimestamp(Process.ts).build()
                                                                    ).build()).build()).build()
                            ));
                        }
                        break;
                    default:
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
}

package consensus.network.process;

import java.util.Map;

public class EventsThread extends Thread {
    private Thread t;
    private String threadName;

    public EventsThread(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        while (true) {
            for (Map.Entry<Integer, ConsensusSystem> entry : Process.systems.entrySet()) { // for each system
                entry.getValue().eventLoop(); // check the events queue
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

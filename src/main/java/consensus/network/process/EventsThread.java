package consensus.network.process;

import consensus.eventsqueue.QueueNode;

public class EventsThread extends Thread {
    private Thread t;
    private String threadName;

    public EventsThread(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        while (true) {
            if (!Process.eventsQueue.isEmpty()) {
                QueueNode currentEvent = Process.eventsQueue.deleteByCondition();
                if (currentEvent != null) {
                    currentEvent.getEvent().handle();
                }
            }

            if (!Process.runForever) {
                break;
            }
        }
        System.exit(0);
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}

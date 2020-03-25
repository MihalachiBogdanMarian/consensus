package consensus.network;

import consensus.eventhandlers.AbstractEvent;

public class EventsThread implements Runnable {
    private Thread t;
    private String threadName;

    public EventsThread(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
//        while (true) {
//            if (!Client.eventsQueue.isEmpty()) {
//                Client.eventsQueue.readByCondition().handle();
//            }
//        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}

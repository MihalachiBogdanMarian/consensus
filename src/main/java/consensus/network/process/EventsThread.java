package consensus.network.process;

public class EventsThread implements Runnable {
    private Thread t;
    private String threadName;

    public EventsThread(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        while (true) {
            if (!Process.eventsQueue.isEmpty()) {
                Process.eventsQueue.deleteByCondition().getEvent().handle();
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

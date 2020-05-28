package consensus.network.process;

public class EventsThread extends Thread {
    private Thread t;
    private String threadName;
    private String systemId;

    public EventsThread(String threadName, String systemId) {
        this.threadName = threadName;
        this.systemId = systemId;
    }

    public void run() {
        ConsensusSystem consensusSystem = null;
        while (true) {
            consensusSystem = Process.systems.get(systemId); // for the thread's system
            consensusSystem.eventLoop(); // check the events queue
            if (Process.systems.get(systemId).stop) {
                break;
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

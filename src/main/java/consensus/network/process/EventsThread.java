package consensus.network.process;

import consensus.eventhandlers.OmegaRecovery;
import consensus.eventsqueue.QueueNode;

import java.util.Timer;
import java.util.TimerTask;

public class EventsThread extends Thread {
    private Thread t;
    private String threadName;
    private static boolean crash = false;
    private static boolean hasCrashed = false;

    public EventsThread(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        while (true) {

//            if (Process.l0 != null && Process.l0.equals(Process.getSelf()) && !hasCrashed) {
//                hasCrashed = true;
//                starttimer(100);
//            }

            if (!Process.eventsQueue.isEmpty()) {
                QueueNode currentEvent = Process.eventsQueue.deleteByCondition();
                if (currentEvent != null) {
                    currentEvent.getEvent().handle();
                }
            }

            if (crash) {
                simulateCrachRecovery(5);
            }

//            if (!Process.runForever) {
//                break;
//            }
        }
//        System.exit(0);
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
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

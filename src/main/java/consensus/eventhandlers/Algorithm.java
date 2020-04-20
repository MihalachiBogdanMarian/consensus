package consensus.eventhandlers;

import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

public interface Algorithm {

    String getName();

    void setName(String name);

    default void init(int systemId) {
        System.out.println(this.getClass().toString() + " initialized...");
    }

    default boolean handle(Message message) {
        System.out.println(this.getClass().toString() + " handled message: " + message.toString());
        return true;
    }

    default void displayExecution(String methodName) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + " executing...");
        }
    }

    default void displayExecution(String methodName, Message message) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + " (Message: " + message.toString().replace("\n", " ") + ") executing...");
        }

    }

    default void displayExecution(String methodName, ProcessId processFrom, Message message) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + " (From: " + processFrom.getIndex()
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }

    default void displayExecution(String methodName, int newtsP, ProcessId newlP) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + " (New timestamp: " + newtsP + ", New leader: " + newlP.getIndex() + ") executing...");
        }
    }

    default void displayExecution(String methodName, int value) {
        synchronized (System.out) {
            if (this.getName().equals("EP")) {
                System.out.println(this.getName() + "." + methodName + "." + value + " executing...");
            } else if (this.getName().equals("UC")) {
                System.out.println(this.getName() + "." + methodName + " (Value: " + value + ") executing...");
            }
        }
    }

    default void displayExecution(String methodName, int ts, int valts, int val) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + "." + ts + ".state(" + valts + ", " + val + ") executing...");
        }
    }

    default void displayExecution(String methodName, int ts, int v) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + "." + ts + " (Value: " + v + ") executing...");
        }
    }

    default void displayExecution(String methodName, int ts, ProcessId l, EpState state) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + "." + ts + " (Process: " + l.getIndex()
                    + ", " + "State(" + state.getTimestamp() + ", " + state.getValue() + ")) executing...");
        }
    }

    default void displayExecution(String methodName, ProcessId process) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + " (Trusted process: " + process.getIndex() + ") executing...");
        }
    }

    default void displayExecution(String methodName, ProcessId processFrom, ProcessId processTo, Message message) {
        synchronized (System.out) {
            System.out.println(this.getName() + "." + methodName + " (From: " + processFrom.getIndex()
                    + ", To: " + processTo.getIndex()
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }
}

package consensus.eventhandlers;

import consensus.protos.Consensus.Message;
import consensus.protos.Consensus.ProcessId;

public interface Algorithm {

    String getName();

    void setName(String name);

    default void init(String systemId) {
        System.out.println(this.getClass().toString() + " initialized...");
    }

    default void init(String systemId, EpState epState) {
        System.out.println(this.getClass().toString() + " initialized...");
    }

    default boolean handle(Message message) {
        System.out.println(this.getClass().toString() + " handled message: " + message.toString());
        return true;
    }

    default boolean match(Message message) {
        System.out.println(this.getClass().toString() + " matched message: " + message.toString());
        return true;
    }

    default void displayExecution(String systemId, String methodName) {
        synchronized (System.out) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " executing...");
        }
    }

    default void displayExecution(String systemId, String methodName, Message message) {
        synchronized (System.out) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }

    default void displayExecution(String systemId, String methodName, ProcessId processFrom, Message message) {
        synchronized (System.out) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (From: " + processFrom.getOwner() + "-" + processFrom.getIndex()
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }

    default void displayExecution(String systemId, String methodName, int newtsP, ProcessId newlP) {
        synchronized (System.out) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (New timestamp: " + newtsP + ", New leader: " + newlP.getOwner() + "-" + newlP.getIndex() + ") executing...");
        }
    }

    default void displayExecution(String systemId, String methodName, int value) {
        synchronized (System.out) {
            if (this.getName().startsWith("EP")) {
                System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + "." + value + " executing...");
            } else if (this.getName().equals("UC")) {
                System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (Value: " + value + ") executing...");
            }
        }
    }

    default void displayExecution(String systemId, String methodName, int ts, int v) {
        synchronized (System.out) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + "." + ts + " (Value: " + v + ") executing...");
        }
    }

    default void displayExecution(String systemId, String methodName, ProcessId process) {
        synchronized (System.out) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (Trusted process: " + process.getOwner() + "-" + process.getIndex() + ") executing...");
        }
    }

    default void displayExecution(String systemId, String methodName, ProcessId processFrom, ProcessId processTo, Message message) {
        synchronized (System.out) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (From: " + processFrom.getOwner() + "-" + processFrom.getIndex()
                    + ", To: " + processTo.getOwner() + "-" + processTo.getIndex()
                    + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
        }
    }
}

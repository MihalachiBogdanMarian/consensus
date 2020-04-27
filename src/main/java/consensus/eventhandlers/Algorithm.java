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

    default void displayExecution(int systemId, String methodName) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " executing...");
    }

    default void displayExecution(int systemId, String methodName, Message message) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (Message: " + message.toString().replace("\n", " ") + ") executing...");
    }

    default void displayExecution(int systemId, String methodName, ProcessId processFrom, Message message) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (From: " + processFrom.getIndex()
                + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
    }

    default void displayExecution(int systemId, String methodName, int newtsP, ProcessId newlP) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (New timestamp: " + newtsP + ", New leader: " + newlP.getIndex() + ") executing...");
    }

    default void displayExecution(int systemId, String methodName, int value) {
        if (this.getName().startsWith("EP")) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + "." + value + " executing...");
        } else if (this.getName().equals("UC")) {
            System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (Value: " + value + ") executing...");
        }
    }

    default void displayExecution(int systemId, String methodName, int ts, int valts, int val) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + "." + ts + ".state(" + valts + ", " + val + ") executing...");
    }

    default void displayExecution(int systemId, String methodName, int ts, int v) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + "." + ts + " (Value: " + v + ") executing...");
    }

    default void displayExecution(int systemId, String methodName, int ts, ProcessId l, EpState state) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + "." + ts + " (Process: " + l.getIndex()
                + ", " + "State(" + state.getTimestamp() + ", " + state.getValue() + ")) executing...");
    }

    default void displayExecution(int systemId, String methodName, ProcessId process) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (Trusted process: " + process.getIndex() + ") executing...");
    }

    default void displayExecution(int systemId, String methodName, ProcessId processFrom, ProcessId processTo, Message message) {
        System.out.println("SYSTEM " + systemId + ": " + this.getName() + "." + methodName + " (From: " + processFrom.getIndex()
                + ", To: " + processTo.getIndex()
                + ", Message: " + message.toString().replace("\n", " ") + ") executing...");
    }
}

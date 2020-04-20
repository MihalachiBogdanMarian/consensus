package consensus.eventsqueue;

import consensus.protos.Consensus.Message;

public class QueueNode {

    private Message event;
    private QueueNode next;

    public QueueNode() {
        event = null;
        next = null;
    }

    public QueueNode(Message event, QueueNode next) {
        this.event = event;
        this.next = next;
    }

    public Message getEvent() {
        return event;
    }

    public void setEvent(Message event) {
        this.event = event;
    }

    public QueueNode getNext() {
        return next;
    }

    public void setNext(QueueNode next) {
        this.next = next;
    }
}

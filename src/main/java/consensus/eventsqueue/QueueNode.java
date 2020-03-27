package consensus.eventsqueue;

import consensus.eventhandlers.AbstractEvent;

public class QueueNode<T extends AbstractEvent> {

    private T event;
    private QueueNode<T> next;

    public QueueNode() {
        event = null;
        next = null;
    }

    public QueueNode(T object, QueueNode<T> next) {
        this.event = object;
        this.next = next;
    }

    public T getEvent() {
        return event;
    }

    public void setEvent(T event) {
        this.event = event;
    }

    public QueueNode<T> getNext() {
        return next;
    }

    public void setNext(QueueNode<T> next) {
        this.next = next;
    }
}

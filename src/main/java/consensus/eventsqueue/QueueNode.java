package consensus.eventsqueue;

import consensus.eventhandlers.AbstractEvent;

public class QueueNode<T extends AbstractEvent> {

    private T object;
    private QueueNode<T> next;

    public QueueNode() {
        object = null;
        next = null;
    }

    public QueueNode(T object, QueueNode<T> next) {
        this.object = object;
        this.next = next;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public QueueNode<T> getNext() {
        return next;
    }

    public void setNext(QueueNode<T> next) {
        this.next = next;
    }
}

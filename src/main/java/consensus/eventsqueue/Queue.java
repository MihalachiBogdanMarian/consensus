package consensus.eventsqueue;

import consensus.eventhandlers.AbstractEvent;

public class Queue<T extends AbstractEvent> {

    private QueueNode<T> head;
    private QueueNode<T> tail;

    public Queue() {
        this.head = null;
        this.tail = null;
    }

    public Queue(T event) {
        this.head = new QueueNode<>(event, null);
        this.tail = this.head;
    }

    public Queue(QueueNode<T> head, QueueNode<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    public void insert(T event) {
        QueueNode<T> queueNode = new QueueNode<>(event, null);
        if (!this.isEmpty()) {
            this.tail.setNext(queueNode);
            this.tail = queueNode;
        } else {
            this.head = queueNode;
            this.tail = this.head;
        }
    }

    public QueueNode<T> deleteByCondition() {
        QueueNode<T> queueNode = this.head;
        QueueNode<T> previousQueueNode = null;
        if (queueNode.getEvent().conditionFulfilled()) {
            this.head = this.head.getNext();
            return queueNode;
        } else {
            while (!queueNode.getEvent().conditionFulfilled()) {
                previousQueueNode = queueNode;
                queueNode = queueNode.getNext();
            }
            assert previousQueueNode != null;
            previousQueueNode.setNext(queueNode.getNext());
            queueNode.setNext(null);
            return queueNode;
        }
    }

    public QueueNode<T> delete() {
        QueueNode<T> queueNode = this.head;
        this.head = this.head.getNext();
        return queueNode;
    }

    public T readByCondition() {
        QueueNode<T> queueNode = this.head;
        while (!queueNode.getEvent().conditionFulfilled()) {
            queueNode = queueNode.getNext();
        }
        return queueNode.getEvent();
    }

    public T read() {
        return this.head.getEvent();
    }

    public void display() {
        Queue<T> copyQueue = new Queue<>(this.head, this.tail);
        while (!copyQueue.isEmpty()) {
            System.out.println(copyQueue.read().toString());
            copyQueue.delete();
        }
    }

    public boolean isEmpty() {
        return this.head == null;
    }

    public QueueNode<T> getHead() {
        return head;
    }

    public void setHead(QueueNode<T> head) {
        this.head = head;
    }

    public QueueNode<T> getTail() {
        return tail;
    }

    public void setTail(QueueNode<T> tail) {
        this.tail = tail;
    }
}

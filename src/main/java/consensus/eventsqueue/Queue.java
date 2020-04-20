package consensus.eventsqueue;

import consensus.network.process.ConsensusSystem;
import consensus.network.process.Process;
import consensus.eventhandlers.AbstractAlgorithm;
import consensus.protos.Consensus.Message;

import java.util.Map;

public class Queue {

    private QueueNode head;
    private QueueNode tail;

    public Queue() {
        this.head = null;
        this.tail = null;
    }

    public Queue(Message event) {
        this.head = new QueueNode(event, null);
        this.tail = this.head;
    }

    public Queue(QueueNode head, QueueNode tail) {
        this.head = head;
        this.tail = tail;
    }

    public void insert(Message event) {
        QueueNode queueNode = new QueueNode(event, null);
        if (!this.isEmpty()) {
            this.tail.setNext(queueNode);
            this.tail = queueNode;
        } else {
            this.head = queueNode;
            this.tail = this.head;
        }
    }

    public QueueNode deleteByCondition() {
        QueueNode queueNode = this.head;
        QueueNode previousQueueNode = null;
        if (match(queueNode.getEvent())) {
            this.head = this.head.getNext();
            return queueNode;
        } else {
            while (!match(queueNode.getEvent())) {
                previousQueueNode = queueNode;
                queueNode = queueNode.getNext();
                if (queueNode == null) {
                    return null;
                }
            }
            assert previousQueueNode != null;
            previousQueueNode.setNext(queueNode.getNext());
            queueNode.setNext(null);
            return queueNode;
        }
    }

    public QueueNode delete() {
        QueueNode queueNode = this.head;
        this.head = this.head.getNext();
        return queueNode;
    }

    public Message readByCondition() {
        QueueNode queueNode = this.head;
        while (!match(queueNode.getEvent())) {
            queueNode = queueNode.getNext();
        }
        return queueNode.getEvent();
    }

    public Message read() {
        return this.head.getEvent();
    }

    public void display() {
        Queue copyQueue = new Queue(this.head, this.tail);
        while (!copyQueue.isEmpty()) {
            System.out.println(copyQueue.read().toString());
            copyQueue.delete();
        }
    }

    private boolean match(Message message) {
        for (Map.Entry<String, AbstractAlgorithm> entry : Process.systems.get(Integer.parseInt(message.getSystemId())).algorithms.entrySet()) {
            if (entry.getValue().handle(message)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.head == null;
    }

    public QueueNode getHead() {
        return head;
    }

    public void setHead(QueueNode head) {
        this.head = head;
    }

    public QueueNode getTail() {
        return tail;
    }

    public void setTail(QueueNode tail) {
        this.tail = tail;
    }
}

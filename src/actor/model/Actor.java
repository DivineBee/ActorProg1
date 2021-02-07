package actor.model;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Beatrice V.
 * @created 04.02.2021 - 11:14
 * @project ActorProg1
 */
public class Actor<Message> implements Runnable {

    private volatile boolean isThreadAlive;
    private boolean isHelperCreated = false;
    boolean isMaster = true;

    private short maxMailboxSize = 1000;
    private int actorId;

    private volatile Thread currentThread = null;

    private volatile BlockingQueue<Message> mailbox;
    private final Behavior<Message> behavior;

    public Actor(int actorId, Behavior<Message> behavior) {
        this.behavior = behavior;
        this.mailbox = new LinkedBlockingQueue<>();
        this.actorId = actorId;
        isThreadAlive = true;
        new Thread(this).start();
    }

    public String getName() { return this.name; }
    Behavior<Message> getBehavior() { return this.behavior; }

    void setMaxMailboxSize (short maxMailboxSize) { this.maxMailboxSize = maxMailboxSize; }

    public void send(Object message) {
        if (message == null) throw new NullPointerException("Can't send null message.");

        BlockingQueue<Message> mailbox = this.mailbox;
        if (mailbox == null) return;

        if (!mailbox.offer(message)) {
            die(new DeadException());
            return;
        }
    }

    public void die(Throwable reason) {
        isThreadAlive = false;
        this.mailbox.clear();
        try {
            interruptCurrentThread(self);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object takeMessage() throws InterruptedException {
        if (Thread.currentThread() != this.currentThread)
            throw new RuntimeException("error!");

        BlockingQueue<Message> mailbox = this.mailbox;
        if (mailbox == null) return null;
        return mailbox.take();
    }

    @Override
    public void run() {
        Object message;
        Behaviour behaviour;

        synchronized (this) {
            if (isThreadAlive) return;

            message = mailbox.poll();
            if (message == null) return;

            isThreadAlive = true;
            currentThread = Thread.currentThread();
        }

    }

    private void interruptCurrentThread(Thread currentThread) {
        if (currentThread == null) return;
        synchronized (this) { currentThread.interrupt(); }
    }
}
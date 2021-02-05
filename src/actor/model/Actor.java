package actor.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
/**
 * @author Beatrice V.
 * @created 04.02.2021 - 11:14
 * @project ActorProg1
 */
public class Actor implements Runnable {
    private final Executor theExecutor;
    private int actorId;

    private volatile BlockingQueue<Object> mailbox;
    private volatile boolean isRunning = false;
    private volatile Thread currentThread = null;
    private volatile boolean panicExit;

    public Actor(Executor executor, BlockingQueue<Object> mailbox, int actorId, boolean panicExit) {
        theExecutor = executor;
        this.mailbox = mailbox;
        this.actorId = actorId;
        isRunning = false;
        this.panicExit = panicExit;
    }

    public void send(Object message) {
        if (message == null) throw new NullPointerException("Can't send null message.");

        BlockingQueue<Object> mailbox = this.mailbox;
        if (mailbox == null) return;

        if (!mailbox.offer(message)) {
            die(new BlockedException());
            return;
        }
        theExecutor.execute(this);
    }

    //need to think
    public void die(Throwable reason) {
        Thread currentThread;

        synchronized (this) {
            currentThread = this.currentThread;
            this.currentThread = null;
            this.mailbox = null;
            this.isRunning = false;
        }
        interruptCurrentThread(currentThread);
    }

    public Object takeNextMessage() throws InterruptedException {
        if (Thread.currentThread() != this.currentThread)
            throw new RuntimeException("error!");

        BlockingQueue<Object> mailbox = this.mailbox;
        if (mailbox == null) return null;
        return mailbox.take();
    }

    @Override
    public void run() {
        Object message;

        synchronized (this) {
            if (isRunning) return;

            message = mailbox.poll();
            if (message == null) return;

            isRunning = true;
            currentThread = Thread.currentThread();
        }
        currentThread = null;

        isRunning = false;
        theExecutor.execute(this);
    }

    private void interruptCurrentThread(Thread currentThread) {
        if (currentThread == null) return;
        synchronized (this) { currentThread.interrupt(); }
    }
}
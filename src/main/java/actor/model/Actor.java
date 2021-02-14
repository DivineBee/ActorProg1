package actor.model;

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

    private short MAX_MAILBOX_SIZE = 1000;
    private String idActor;

    private volatile BlockingQueue<Message> mailbox;
    private final Behaviour<Message> behavior;

    public Actor(String idActor, Behaviour<Message> behavior) {
        this.behavior = behavior;
        this.mailbox = new LinkedBlockingQueue<>();
        this.idActor = idActor;
        isThreadAlive = true;
        new Thread(this).start();
    }

    public String getName() {
        return this.idActor;
    }

    Behaviour<Message> getBehavior() {
        return this.behavior;
    }

    void setMaxMailboxSize(short MAX_MAILBOX_SIZE) {
        this.MAX_MAILBOX_SIZE = MAX_MAILBOX_SIZE;
    }

    public void die() {
        isThreadAlive = false;
        this.mailbox.clear();
        try {
            Manager.actorDie(this.idActor, this.isMaster);// ЕСЛИ МАСТЕР ТО ВОСКРЕСНЕТ
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean takeMessage(Message message) throws DeadException {
        if (!isThreadAlive) {
            throw new DeadException();
        }

        short currentMailboxSize = (short) mailbox.size();

        if (!isHelperCreated && (currentMailboxSize > MAX_MAILBOX_SIZE)) {
            if (Manager.createHelper(this.idActor, this.behavior, this.MAX_MAILBOX_SIZE))
                isHelperCreated = true;
        }

        if (isHelperCreated && currentMailboxSize > MAX_MAILBOX_SIZE) {
            if (!Manager.sendMessage(this.idActor + Manager.HELPER_NAME, message)) // мастер пытается кинуть сообщение своему помощнику
                isHelperCreated = false;
        }

        return mailbox.offer(message);
    }

    @Override
    public void run() {
        try {
            while (behavior.onReceive(this, mailbox.take()) && isThreadAlive) {
                if (!isMaster && mailbox.size() == 0) {
                    die();
                    break;
                }
            }
        } catch (InterruptedException exception) {
            behavior.onException(this, exception);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void interruptCurrentThread(Thread currentThread) {
        if (currentThread == null) return;

        synchronized (this) {
            currentThread.interrupt();
        }
    }

    public void sleepActor(){
        try {
            short max = 500;
            short min = 50;
            int range = max - min + 1;
            Thread.sleep((long)(Math.random() * range) + min);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
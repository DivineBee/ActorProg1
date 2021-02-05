package actor.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
/**
 * @author Beatrice V.
 * @created 04.02.2021 - 18:45
 * @project ActorProg1
 */
public class ActorBuilder {
    private Executor defaultExecutor;

    private Executor theExecutor;
    private int mailboxSize;
    private boolean panicExit;
    private int actorId;

    public static int MAILBOX_SIZE = 10000;

    public ActorBuilder() {
        defaultExecutor = makeDefaultExecutor();
        reset();
    }

    public Actor build() {
        if (mailboxSize <= 0) throw new IllegalArgumentException("The mailbox must have a positive size");
        if (theExecutor == null) throw new NullPointerException("Expected an executor.");
        if (actorId <0 ) throw new IllegalArgumentException("Id can't be negative.");

        BlockingQueue<Object> mailbox = new ArrayBlockingQueue<>(mailboxSize);
        reset();
        return new Actor( theExecutor,  mailbox, actorId, panicExit);
    }

    public ActorBuilder executor(Executor e) {
        theExecutor = e;
        return this;
    }

    public ActorBuilder mailboxSize(int size) {
        mailboxSize = size;
        return this;
    }

    public ActorBuilder panicExit(boolean flag) {
        panicExit = flag;
        return this;
    }

    public void setDefaultExecutor(Executor e) {
        this.defaultExecutor = e;
    }

    private void reset() {
        theExecutor = defaultExecutor;
        mailboxSize = MAILBOX_SIZE;
        panicExit = false;
    }

    private static Executor makeDefaultExecutor() {
        return Executors.newCachedThreadPool();
    }
}
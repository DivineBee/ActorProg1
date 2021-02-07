package actor.model;

/**
 * @author Beatrice V.
 * @created 05.02.2021 - 17:28
 * @project ActorProg1
 */
public interface Behaviour<Message> {
    public Behaviour run(Actor self, Object message) throws Throwable;
}

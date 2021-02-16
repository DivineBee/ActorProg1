package actor.model;

import java.util.HashMap;

/**
 * @author Beatrice V.
 * @created 16.02.2021 - 15:51
 * @project ActorProg1
 */
public class ActorFactory {
    public static final HashMap<String, Actor> actorPool = new HashMap<>();

    public static HashMap<String, Actor> getActorPool() {
        return actorPool;
    }

    public static void setMaxMailboxSize(String nameOfActor, short MAX_MAILBOX_SIZE) {
        actorPool.get(nameOfActor).setMaxMailboxSize(MAX_MAILBOX_SIZE);
    }

    public static void createActor(String idActor, Behaviour behavior) {
        Actor actor = new Actor(idActor, behavior);
        actorPool.put(idActor, actor);
    }
}

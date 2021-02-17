package actor.model;

import static actor.model.Actor.sleepActor;
import static actor.model.ActorFactory.actorPool;
import static actor.model.ActorFactory.createActor;

/**
 * @author Beatrice V.
 * @created 16.02.2021 - 15:51
 * @project ActorProg1
 */
public class ActorScale {
    public static String HELPER_NAME = "helper";
    static final short MAX_AMOUNT_OF_ACTORS = 100;

    public static boolean createHelper(String idMasterActor, Behaviour masterBehaviour, short masterMaxMessages) {
        if (actorPool.size() > ActorScale.MAX_AMOUNT_OF_ACTORS) {
            return false;
        }
        sleepActor();
        String idHelper = idMasterActor + HELPER_NAME;
        Actor actor = new Actor(idHelper, masterBehaviour);

        actor.setMaxMailboxSize(masterMaxMessages);
        actor.isMaster = false; // помощник самоубивается как только закончит работу а мастер остается в живых даже при завершении работы

        actorPool.put(idHelper, actor);
        return true;
    }
}

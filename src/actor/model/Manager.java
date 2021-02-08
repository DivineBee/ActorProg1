package actor.model;

import java.util.HashMap;

/**
 * @author Beatrice V.
 * @created 07.02.2021 - 19:14
 * @project ActorProg1
 */
public class Manager {
    public static String HELPER_NAME = "helper";
    static final short MAX_AMOUNT_OF_ACTORS = 100;

    private static final HashMap<String, Actor> actorPool = new HashMap<>();

    public static HashMap<String, Actor> getActorPool() {
        return actorPool;
    }

    public static void setMaxMailboxSize(String nameOfActor, short MAX_MAILBOX_SIZE) {
        actorPool.get(nameOfActor).setMaxMailboxSize(MAX_MAILBOX_SIZE);
    }

    public static void actorDie(String idActor, boolean isRespawn) {
        if (isRespawn) {
            Behaviour deathBehaviour = actorPool.get(idActor).getBehavior();
            actorPool.remove(idActor);
            createActor(idActor, deathBehaviour);
        } else {
            actorPool.remove(idActor);
        }
    }

    public static void createActor(String idActor, Behaviour behavior) {
        Actor actor = new Actor(idActor, behavior);
        actorPool.put(idActor, actor);
    }

    public static boolean sendMessage(String idReceiver, Object message) throws DeadException {
        Actor<Object> receiver = actorPool.get(idReceiver);

        if (receiver == null) {
            System.err.println("Don't have this actor --> " + receiver);
        } else if (!receiver.takeMessage(message)) { //если не может принять сообщение
            System.err.println("Actor " + idReceiver + " can't receive message");
            return false;
        }
        return true;
    }

    public static boolean createHelper(String idMasterActor, Behaviour masterBehaviour, short masterMaxMessages) {
        if (actorPool.size() > Manager.MAX_AMOUNT_OF_ACTORS) {
            return false;
        }

        String idHelper = idMasterActor + HELPER_NAME;
        Actor actor = new Actor(idHelper, masterBehaviour);

        actor.setMaxMailboxSize(masterMaxMessages);
        actor.isMaster = false; // помощник самоубивается как только закончит работу а мастер остается в живых даже при завершении работы

        actorPool.put(idHelper, actor);
        return true;
    }
}
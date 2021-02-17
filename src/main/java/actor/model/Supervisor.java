package actor.model;

import static actor.model.ActorFactory.actorPool;
import static actor.model.ActorFactory.createActor;

/**
 * @author Beatrice V.
 * @created 16.02.2021 - 15:50
 * @project ActorProg1
 */
public class Supervisor {

    public static void actorDie(String idActor, boolean isRespawn) {
        if (isRespawn) {
            Behaviour deathBehaviour = actorPool.get(idActor).getBehavior();
            actorPool.remove(idActor);
            createActor(idActor, deathBehaviour);
        } else {
            actorPool.remove(idActor);
        }
    }

    public static boolean sendMessage(String idReceiver, Object message) throws DeadException {
        //sleepActor();
        Actor<Object> receiver = actorPool.get(idReceiver);

        if (receiver == null) {
            System.err.println("Don't have this actor --> " + receiver);
        } else if (!receiver.takeMessage(message)) { //если не может принять сообщение
            System.err.println("Actor " + idReceiver + " can't receive message");
            return false;
        }
        return true;
    }
}

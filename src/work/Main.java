package work;

import actor.model.DeadException;
import actor.model.Manager;
import behaviours.SSEClientBehaviour;

/**
 * @author Beatrice V.
 * @created 07.02.2021 - 19:36
 * @project ActorProg1
 */
public class Main {
    public static void main(String[] args) throws DeadException {
        SSEClientBehaviour sseClientBehaviour = new SSEClientBehaviour();

        Manager.createActor("firstSSEClient", sseClientBehaviour);
        Manager.createActor("secondSSEClient", sseClientBehaviour);

       // Manager.createActor("jsonHandler", jsonBehaviour);

        Manager.sendMessage("firstSSEClient", "http://localhost:4000/tweets/1");
        Manager.sendMessage("secondSSEClient", "http://localhost:4000/tweets/2");
    }
}

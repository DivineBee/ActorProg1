package work;

import actor.model.DeadException;
import actor.model.Manager;
import behaviours.JSONBehaviour;
import behaviours.SSEClientBehaviour;

/**
 * @author Beatrice V.
 * @created 07.02.2021 - 19:36
 * @project ActorProg1
 */
public class Main {
    public static void main(String[] args) throws DeadException {
        SSEClientBehaviour sseClientBehaviour = new SSEClientBehaviour();
        JSONBehaviour jsonBehaviour = new JSONBehaviour();

        Manager.createActor("firstSSEClient", sseClientBehaviour);
        Manager.createActor("secondSSEClient", sseClientBehaviour);

        Manager.sendMessage("firstSSEClient", "http://localhost:4000/tweets/1");
        Manager.sendMessage("secondSSEClient", "http://localhost:4000/tweets/2");

       // Manager.sendMessage("jsonHandler", "http://localhost:4000/tweets/1");
    }
}

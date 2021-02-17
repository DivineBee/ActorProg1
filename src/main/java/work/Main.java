package work;

import actor.model.ActorFactory;
import actor.model.DeadException;
import actor.model.Supervisor;
import behaviours.EmotionHandler;
import behaviours.SSEClientBehaviour;

/**
 * @author Beatrice V.
 * @created 07.02.2021 - 19:36
 * @project ActorProg1
 */
public class Main {
    public static void main(String[] args) throws DeadException {
        SSEClientBehaviour sseClientBehaviour = new SSEClientBehaviour();
        EmotionHandler emotionHandler = new EmotionHandler();

        ActorFactory.createActor("firstSSEClient", sseClientBehaviour);
        ActorFactory.createActor("secondSSEClient", sseClientBehaviour);
        ActorFactory.createActor("em", emotionHandler);

        Supervisor.sendMessage("firstSSEClient", "http://localhost:4000/tweets/1");
        Supervisor.sendMessage("secondSSEClient", "http://localhost:4000/tweets/2");
        Supervisor.sendMessage("em", "B:\\PROGRAMMING\\PROJECTS\\ActorProg1\\src\\main\\resources\\emotions.txt");
    }
}

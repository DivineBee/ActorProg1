package work;

import actor.model.Actor;
import actor.model.Behaviour;
import actor.model.DeadException;
import actor.model.Manager;

import java.io.EOFException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Beatrice V.
 * @created 07.02.2021 - 19:36
 * @project ActorProg1
 */
public class Main {
    public static Behaviour<String> sseClientBehaviour = new Behaviour<String>() {
        @Override
        public boolean onReceive(Actor<String> self, String urlPath) throws Exception {
            try {
                URL sseUrl = new URL(urlPath);
                SSEClient client = new SSEClient(sseUrl, new SSEClient.EventListener() {
                    int count = 0;
                    public void message(SSEClient.SSEEvent evt) {
                        String data = evt.data;
                        System.out.println(("Client " + self.getName() + data));
                        //Manager.sendMessage("jsonHandler", data);
                    }
                });
                client.connect();
            } catch (EOFException | MalformedURLException e) {
                    System.out.println("the stream ended!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            return false;
        }

        @Override
        public void onException(Actor<String> self, Exception exc) {
            exc.printStackTrace();
            self.die();
        }
    };

    public static void main(String[] args) throws DeadException {
        Manager.createActor("firstSSEClient", sseClientBehaviour);
        Manager.createActor("secondSSEClient", sseClientBehaviour);

       // Manager.createActor("jsonHandler", jsonBehaviour);

        Manager.sendMessage("firstSSEClient", "http://localhost:4000/tweets/1");
        Manager.sendMessage("secondSSEClient", "http://localhost:4000/tweets/2");
    }
}

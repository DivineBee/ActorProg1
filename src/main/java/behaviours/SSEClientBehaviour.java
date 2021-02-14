package behaviours;

import actor.model.Actor;
import actor.model.Behaviour;
import actor.model.DeadException;
import actor.model.Manager;
import work.SSEClient;

import java.io.EOFException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Beatrice V.
 * @created 08.02.2021 - 18:39
 * @project ActorProg1
 */
public class SSEClientBehaviour implements Behaviour<String> {
    @Override
    public boolean onReceive(Actor<String> self, String urlPath) throws Exception {
        try {
            URL sseUrl = new URL(urlPath);
            SSEClient client = new SSEClient(sseUrl, new SSEClient.EventListener() {
                int count = 0;
                public void message(SSEClient.SSEEvent evt) throws DeadException {
                    String data = evt.data;

                    if(data.contains("{\"message\": panic}")) {
                        System.out.println("Actor died x_x");
                        self.die();
                    }
                    //System.out.println(("Client " + self.getName() + data));
                    JSONBehaviour jsonBehaviour = new JSONBehaviour();
                    Manager.createActor("jsonHandler", jsonBehaviour);
                    Manager.sendMessage("jsonHandler", data);
                }
            });
            client.connect();
            self.sleepActor();
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
}

package behaviours;


import actor.model.Actor;
import actor.model.Behaviour;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Beatrice V.
 * @created 10.02.2021 - 14:14
 * @project ActorProg1
 */
public class JSONBehaviour implements Behaviour<String> {
    public static String tweet = null;

    @Override
    public boolean onReceive(Actor<String> self, String data) throws Exception {
        ObjectMapper jsonMapper = new ObjectMapper();

        if (data.contains("{\"message\": panic}")) {
            System.out.println("Actor died x_x");
            self.die();
            return false;
        }

        if (!data.contains("localhost") && data != null && !data.isEmpty()) {
            JsonNode jsonNode = jsonMapper.readValue(data, JsonNode.class);
            System.out.println("DATA--- " + jsonNode);

            JsonNode tweetNode = jsonNode.get("message").get("tweet").get("text");
            tweet = tweetNode.asText();

            JsonNode userNode = jsonNode.get("message").get("tweet").get("user").get("screen_name");
            String user = userNode.asText();

            System.out.println("USER: " + user + " | " + "TWEET: " + tweet + " |\n");
        }
        return true;
    }

    @Override
    public void onException(Actor<String> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}

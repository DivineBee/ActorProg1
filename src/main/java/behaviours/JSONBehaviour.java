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
    public static String user = null;

    @Override
    public boolean onReceive(Actor<String> self, String data) throws Exception {
        // create ObjectMapper which will be used to parse the json file
        ObjectMapper jsonMapper = new ObjectMapper();

        // if the data contains panic message then the actor is killing itself via die()
        if (data.contains("{\"message\": panic}")) {
            System.out.println("Actor died x_x");
            self.die();
            return false;
        }

        // if the data is not null and is not localhost then parse and extract needed fields
        if (!data.contains("localhost") && data != null && !data.isEmpty()) {
            // read data and assign to jsonNode
            JsonNode jsonNode = jsonMapper.readValue(data, JsonNode.class);

            // extract tweet and save to variable
            JsonNode tweetNode = jsonNode.get("message").get("tweet").get("text");
            tweet = tweetNode.asText() + " ";

            // extract user and save to variable
            JsonNode userNode = jsonNode.get("message").get("tweet").get("user").get("screen_name");
            user = userNode.asText();

            // print beautifully the output
            System.out.println("USER: " + user + " | " + "TWEET: " + tweet + " | " + "SCORE: " + EmotionHandler.getEmotionScore(tweet));
        }
        return true;
    }

    // if exception encountered then actor will die
    @Override
    public void onException(Actor<String> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}

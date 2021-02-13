package behaviours;


import actor.model.Actor;
import actor.model.Behaviour;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import work.Tweet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Beatrice V.
 * @created 10.02.2021 - 14:14
 * @project ActorProg1
 */
public class JSONBehaviour implements Behaviour<String> {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public boolean onReceive(Actor<String> self, String data) throws Exception {
        System.out.println("data" + data);

        if(data.contains("{\"message\": panic}")) {
            System.out.println("Actor died x_x");
            self.die();
            return false;
        }

       /* HashMap<String, Object> mapOfRawData = new HashMap<>();
        if (!data.contains("localhost")) {
            Map map = jsonMapper.readValue(data.substring(12), Map.class);
            System.out.println("map" + map);
        }*/
        if (!data.contains("localhost")) {
            JsonNode jsonNode = jsonMapper.readValue(data, JsonNode.class);
            System.out.println("node " + jsonNode);

            /*JsonNode tweetNode = jsonNode.get("tweet");
            String tweet = tweetNode.asText();
            System.out.println("tweet = " + tweet);*/

            JsonNode child = jsonNode.get("tweet");
            JsonNode childField = child.get("user");
            String field = childField.asText();
            System.out.println("field = " + field);
        }
        return false;
    }

    @Override
    public void onException(Actor<String> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}

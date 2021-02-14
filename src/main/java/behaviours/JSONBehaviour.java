package behaviours;


import actor.model.Actor;
import actor.model.Behaviour;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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

    @Override
    public boolean onReceive(Actor<String> self, String data) throws Exception {
        /*if (data==null || data.isEmpty()){
            return false;
        }*/

        ObjectMapper jsonMapper = new ObjectMapper();
        //System.out.println("data" + data);
        if(data.contains("{\"message\": panic}")) {
            System.out.println("Actor died x_x");
            self.die();
            return false;
        }

        /*try {
            if (!data.contains("localhost")) {
                Map map = jsonMapper.readValue(data.substring(12), Map.class);
                //System.out.println("map" + map);
                System.out.println("------" + map.get("tweet"));
                System.out.println("");

            }
        }catch (StringIndexOutOfBoundsException e){
            System.err.println("No message");
        }*/
        if (!data.contains("localhost")) {
            try {
                JsonNode jsonNode = jsonMapper.readValue(data, JsonNode.class);
                System.out.println("node " + jsonNode);

                JsonNode tweetNode = jsonNode.get("message").get("tweet").get("text");
                String tweet = tweetNode.asText();
                System.out.println("tweet = " + tweet);
            } catch (MismatchedInputException e){
                System.err.println("error" + data);
            }
        }
        return true;
    }

    @Override
    public void onException(Actor<String> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}

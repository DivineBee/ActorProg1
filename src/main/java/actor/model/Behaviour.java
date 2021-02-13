package actor.model;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @author Beatrice V.
 * @created 05.02.2021 - 17:28
 * @project ActorProg1
 */
public interface Behaviour<Message> {
    boolean onReceive(Actor<Message> self, Message message) throws Exception;
    void onException(Actor<Message> self, Exception exc);
}

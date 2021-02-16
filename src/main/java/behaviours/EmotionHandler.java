package behaviours;
/**
 * @author Beatrice V.
 * @created 15.02.2021 - 10:32
 * @project ActorProg1
 */

import actor.model.Actor;
import actor.model.Behaviour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import static behaviours.JSONBehaviour.tweet;

public class EmotionHandler implements Behaviour<String> {
    public static final HashMap<String, Integer> emotionsMap = new HashMap<String, Integer>();

    public void getEmotionScore(HashMap<String, Integer> map) {
        //  emotion score
        int score = 0;

        // for every word/phrase from emotionMap do the next thing:
        for (String emotionWord : map.keySet()) {
            // if the word/phrase is contained inside the tweet:
            if (tweet.contains(emotionWord)) {
                // total score = number of word appearances from emotionsMap * score number for that word
                score += amountOfEmotionWordAppearancesInTweet(tweet, emotionWord) * map.get(emotionWord);
            }
        }
        System.out.println("SCORE " + score);

    }

    public int amountOfEmotionWordAppearancesInTweet(String tweet, String emotionWord) {

        String arrayOfTweets[] = tweet.split(" ");  // split the string by spaces and put in array
        int count = 0;  // search for given word in array

        for (int i = 0; i < arrayOfTweets.length; i++) {
            // if match found increase count
            if (emotionWord.equals(arrayOfTweets[i]))
                count++;
        }
        return count;
    }

    @Override
    public boolean onReceive(Actor<String> self, String s) throws Exception {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(s));
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            line = line.replaceAll("\\s+", " ");
            String[] parts = line.split(" ");
            if (parts.length == 2) {
                String key = parts[0];
                Integer value = Integer.parseInt(parts[1]);
                emotionsMap.put(key, value);
            } else if (parts.length >= 3) {
                String key = "";
                for (int i = 0; i < parts.length - 1; i++) {
                    if (key.isEmpty()) {
                        key = key.concat(parts[i]);
                    } else {
                        key = key.concat(" " + parts[i]);
                    }
                }
                Integer value = Integer.parseInt(parts[parts.length - 1]);
                emotionsMap.put(key, value);
            } else {
                System.out.println("ignoring line: " + line);
            }
        }

        for (String key : emotionsMap.keySet()) {
            System.out.println(key + ":" + emotionsMap.get(key));
        }
        reader.close();
        return false;
    }

    @Override
    public void onException(Actor<String> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}

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

import static behaviours.JSONBehaviour.user;

public class EmotionHandler implements Behaviour<String> {
    // key-value pairs for emotions with their points
    public static final HashMap<String, Integer> emotionsMap = new HashMap<String, Integer>();

    // method which calculates the score adding up occurrences of words in the tweet
    // from the emotionsMap
    public static int getEmotionScore(String tweet) {
        //  emotion score
        int score = 0;

        // for every word/phrase from emotionMap do the next thing:
        for (String emotionWord : emotionsMap.keySet()) {
            String lowerCaseTweet = tweet.toLowerCase();
            // if the word/phrase is contained inside the tweet:
            if (lowerCaseTweet.contains(emotionWord)) {
                // total score = number of word appearances from emotionsMap * score number for that word
                score += amountOfEmotionWordAppearancesInTweet(tweet, emotionWord) * emotionsMap.get(emotionWord);
            }
        }

        if (score == 0) {
            System.out.println("\n" + user + ": IS NEUTRAL");
        } else if (score > 0 && score < 3) {
            System.out.println("\n" + user + ": IS SLIGHTLY HAPPY");
        } else if (score > 3 && score < 7) {
            System.out.println("\n" + user + ": IS HAPPY");
        } else if (score > 7) {
            System.out.println("\n" + user + ": IS VERY HAPPY");
        } else if (score < 0 && score > -3) {
            System.out.println("\n" + user + ": IS SLIGHTLY SAD");
        } else if (score < -3 && score > -7) {
            System.out.println("\n" + user + ": IS SAD");
        } else if (score < -7) {
            System.out.println("\n" + user + ": IS VERY SAD");
        } else {
            System.out.println("\n" + user + ": CAN'T IDENTIFY EMOTION");
        }

        return score;
    }


    public static int amountOfEmotionWordAppearancesInTweet(String tweet, String emotionWord) {
        String reviewableFragment = "";
        int counter = 0;

        for (int startIndex = 0; startIndex < tweet.length() - emotionWord.length(); startIndex++) {
            int endingIndex = startIndex + emotionWord.length();
            if (startIndex != 0 && endingIndex != tweet.length()){
                if (verifyWordBounds(tweet.charAt(startIndex - 1), tweet.charAt(endingIndex))){
                    reviewableFragment = tweet.substring(startIndex, endingIndex);
                    //System.out.println("HERE " + reviewableFragment + " | " + emotionWord);
                    if (reviewableFragment.equalsIgnoreCase(emotionWord)) {
                        counter++;
                    }
                }
            } else if(endingIndex != tweet.length()-1){
                if(verifyWordOneWayBound(tweet.charAt(endingIndex + 1))){
                    reviewableFragment = tweet.substring(startIndex, endingIndex);
                    if (reviewableFragment.equalsIgnoreCase(emotionWord)) {
                        counter++;
                    }
                }
            }
        }
        return counter;
    }

    public static boolean verifyWordBounds(char starting, char ending) {
        if (starting == ' ' || starting == '.' || starting == ',' || starting == '!'
                || starting == '?' || starting == ';' || starting == '#') {
            if(ending == ' ' || ending == '.' || ending == ',' || ending == '!' || ending == '?' || ending == ';'){
                return true;
            }
        }
        return false;
    }

    public static boolean verifyWordOneWayBound(char bound) {
        if (bound == ' ' || bound == '.' || bound == ',' || bound == '!' || bound == '?' || bound == ';') {
            return true;
        }
        return false;
    }


    // Mapping of emotions to keys and their points as values.
    // Here is taken into consideration that some emotions are composed from more
    // than one word (e.g dont like)
    @Override
    public boolean onReceive(Actor<String> self, String s) throws Exception {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(s));
        // while we have something to read
        while ((line = reader.readLine()) != null) {
            // remove whitespaces from beginning and end
            line = line.trim();
            // replace multiple whitespaces occurences with only one
            line = line.replaceAll("\\s+", " ");
            // temporary array for words
            String[] parts = line.split(" ");
            // if there are only 2 items then first is key and second value
            // and attach them to map
            if (parts.length == 2) {
                String key = parts[0];
                Integer value = Integer.parseInt(parts[1]);
                emotionsMap.put(key, value);
                // if more than 2 items then concatenate all the items except last one
                // and add whitespace back
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

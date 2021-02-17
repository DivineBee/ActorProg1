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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static behaviours.JSONBehaviour.user;

public class EmotionHandler implements Behaviour<String> {
    public static final HashMap<String, Integer> emotionsMap = new HashMap<String, Integer>();

    public static int getEmotionScore(String tweet) {
        //  emotion score
        int score = 0;

        // for every word/phrase from emotionMap do the next thing:
        for (String emotionWord : emotionsMap.keySet()) {
            // if the word/phrase is contained inside the tweet:
            if (tweet.contains(emotionWord)) {
                // total score = number of word appearances from emotionsMap * score number for that word
                score += amountOfEmotionWordAppearancesInTweet(tweet, emotionWord) * emotionsMap.get(emotionWord);
            }
        }

        if(score==0){
            System.out.println("\n" + user + ": IS NEUTRAL");
        } else if (score > 0 && score < 3){
            System.out.println("\n" + user + ": IS SLIGHTLY HAPPY");
        } else if (score > 3 && score < 7) {
            System.out.println("\n" + user + ": IS HAPPY");
        } else if (score > 7) {
            System.out.println("\n" + user + ": IS VERY HAPPY");
        } else if (score < 0 && score > -3) {
            System.out.println("\n" + user + ": IS SLIGHTLY SAD");
        } else if (score < -3 && score > -7){
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

        for(int startIndex = 0; startIndex < tweet.length() - emotionWord.length(); startIndex++) {
            reviewableFragment = tweet.substring(startIndex, startIndex + emotionWord.length());
            if(reviewableFragment.equalsIgnoreCase(emotionWord)) {
                counter++;
            }
        }
        return counter;

        /*String[] tweetChunks = tweet.split(" ");

        for (String chunk : tweetChunks) {
            System.out.println("CHUNK -----" + chunk);
            System.out.println("LENGTH" + tweetChunks.length);
        }
        if (tweetChunks.length > 1) {
            return tweetChunks.length - 1;
        }
        return 0;*/
        /*String regex = "\\s|[.,!?;]";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emotionWord);

        boolean matchFound = matcher.find();*/
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

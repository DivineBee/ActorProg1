## Streaming Twitter sentiment analysis system

> Laboratory work No1 at Real-Time Programming  
> University: Technical University of Moldova  
> Faculty: Software Engineering  
> Teacher: Burlacu Alexandru  
> Group: FAF -182  
> Student: Vizant Beatrice  
> Task: Streaming Twitter sentiment analysis system  

## Table of contents

- [Streaming Twitter sentiment analysis system](#streaming-twitter-sentiment-analysis-system)
- [Table of contents](#table-of-contents)
- [Requirements](#requirements)
- [Output example](#output-example)
- [Explanation](#explanation)
- [Actor class](#actor-class)
- [Actor Factory](#actor-factory)
- [Supervisor](#supervisor)
- [Actor Scale](#actor-scale)
- [Behaviour](#behaviour)
- [Implementation](#implementation)
- [Technologies](#technologies)
- [Status](#status)

## Requirements
Implementation of actor model for real-time programming.  
TO-DO:  
* To read 2 SSE streams of actual Twitter API tweets in JSON format.  
* The streams are available from a Docker container, alexburlacu/rtp-server:faf18x on port 4000  
* To make things interesting, the rate of messages varies by up to an order of magnitude, from 100s to 1000s.  
* Route the messages to a group of workers that need to be auto-scaled, you will need to scale up the workers  
(have more) when the rate is high, and less actors when the rate is low  
* Route/load balance messages among worker actors in a round robin fashion  
* Occasionally you will receive "kill messages", on which you have to crash the workers.  
* To continue running the system you will have to have a supervisor/restart policy for the workers.  
* The worker actors also must have a random sleep, in the range of 50ms to 500ms, normally distributed. This  
is necessary to make the system behave more like a real one + give the router/load balancer a bit of a hard  
time + for the optional speculative execution. The output will be shown as log messages.

OPTIONAL:  
* Speculative execution of slow tasks, for some details check the recording of the first lesson  
* "Least connected" load balancing, check for some examples:  
https://blog.envoyproxy.io/examining-load-balancing-algorithms-with-envoy-1be643ea121c
* Have a metrics endpoint to monitor the stats on ingested messages, average execution time, 75th, 90th, 95th  
percentile execution time, number of crashes per given time window, etc  
* Anything else, like the most popular hashtag up until now, or maybe other analytics  

## Output example 
![alt-text](https://github.com/DivineBee/ActorProg1/blob/master/src/main/resources/lab1.gif?raw=true)

## Explanation  
For this laboratory work I decided to make my own Actor system because of multiple reasons. First is that I wanted to better understand how they work and why, also to "feel" them. Second because I wanted to challenge myself and my programming skills, it turned out to be very interesting and fun. So I will describe first how I modeled my actor system and then how it was applied it to the laboratory work.

## Actor class

First of all I created an _Actor_ class which contains a mailbox(a _BlockingQueue_ for holding messages) and set a maximal size of 1000 messages(so if the number of messages will be above this value a helper actor will be created but about this later on.). The class contains an actor id or in other words the name of the actor I called it _idActor_ because later on in more complex projects there might be used number identifiers for actors (when there are a lot of them) but for now they have just names given on creation. One more thing it has _Behaviour_ instance which defines how the actor should process the message, it may vary for each actor. Other fields in the class are self-explanatory like(isThreadAlive, isHelperCreated, isMaster) which are all of boolean type. Their uses will be explained in the methods.  

First method in this class is _die()_ it kills the actor and notifies the Supervisor about this action. Also in my system are two types of Actors - masters and helpers. Masters are the actors which are stored in a sort of _"ThreadPool"_ but called in my case _"ActorPool"_ or by the name "DeadPool" who knows:). So the masters actors upon finishing the tasks are returned to this so called pool meanwhile the helpers are created by other actors, and they "die" when they finish their task/work. That's why inside the die method a check is performed for checking what type of actor is "permitted" to die.
```java
    public void die() {
        // make thread dead(put false flag)
        isThreadAlive = false;
        // free up space and clear mailbox
        this.mailbox.clear();
        try {
            // masters can't die so they will respawn
            Supervisor.actorDie(this.idActor, this.isMaster);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```  
The next method inside the Actor is _takeMessage()_ which's scope is to receive messages. It first checks if actor is alive at all, then performs some checks which will guarantee correct receiving and message processing. First check is if a helper wasn't already created and if mailbox size is over capacity, then request for helper then through actor scaling, a helper is created with same attributes as parent to handle the messages:  
```java
if (ActorScale.createHelper(this.idActor, this.behavior, this.MAX_MAILBOX_SIZE)) {
    isHelperCreated = true;}
```  
And if there is a helper and mailbox is full then retransmit message to helper.
```java
// master tries to send the message to his helper
if (!Supervisor.sendMessage(this.idActor + ActorScale.HELPER_NAME, message)) {
    isHelperCreated = false;}}
// add message to the mailbox
return mailbox.offer(message);
```
Last but not least the _run()_ method from _Runnable_ is overridden, it processes the mailbox and checks if actor killing is required. It takes message from the queue and then kills the helper if it's done with the task.  
```java
while (behavior.onReceive(this, mailbox.take()) && isThreadAlive) {
    // destroy and stop helper if it's done with the task
       if (!isMaster && mailbox.size() == 0) {
          die();
          break;}} ...
```
The last method of this class is _sleepActor()_ which puts actor to sleep by a time interval between 50 and 500ms.  

## Actor Factory
The name of this class us self-explanatory but well, inspired by creational design patterns. It has only 2 responsabilities - to create actors and to set them mailboxSizes. Also this class stores the actorPool variable for convenience, like here is the creation and after the actors are put in the pool so makes sense. The _createActor()_:  
```java
// creates actor and save it in system with its name and behaviour
    public static void createActor(String idActor, Behaviour behavior) {
        Actor actor = new Actor(idActor, behavior);
        actorPool.put(idActor, actor);
    }
```
## Supervisor  
So generally supervision describes a dependency relationship between actors: the supervisor delegates tasks to actors and therefore must respond to their failures. When an actor (i.e. throws an  exception), it suspends itself and sends a message to the supervisor, signaling failure. So the _Supervisor_ removes the dead actor from the system and restarts the actor if it's master, I called this action "respawn" guess why:). It looks like this:  
```java
public static void actorDie(String idActor, boolean isRespawn) {
        if (isRespawn) {
            Behaviour deathBehaviour = actorPool.get(idActor).getBehavior();
            actorPool.remove(idActor);
            createActor(idActor, deathBehaviour);
        } else {
            actorPool.remove(idActor);
        }
```
Also supervisor as said he describes the relationships with actors so it also contains a _sendMessage()_ which sends  
a message to the actor and check message to be received.
```java
public static boolean sendMessage(String idReceiver, Object message) throws DeadException {
        Actor<Object> receiver = actorPool.get(idReceiver);

        if (receiver == null) {
            System.err.println("Don't have this actor --> " + receiver);
        } else if (!receiver.takeMessage(message)) { // if it can't receive the message
            System.err.println("Actor " + idReceiver + " can't receive message");
            return false;
        }
        return true;
    }
```
## Actor Scale  
This class is responsible for scaling the system. In case that the main master actors can't face the pressure - hah weaklings(by can't face the pressure I mean that there are too many messages, and they don't keep up, and the mailboxes are full). In this case the actor scaler comes to help by creating helper actors. This works in the following way: First, once the Actor's mailbox is full the scaler creates a helper for that actor which takes in the actor's name, behaviour and messages. Then check if the actor pool size is not full, if it is then the request of creating helper is denied and the helper will not be created:  
```java
if (actorPool.size() > ActorScale.MAX_AMOUNT_OF_ACTORS) {
            return false;}
``` 
But if everything is fine and actorPool is available then give permission to create a helper for that actor.  
```java
String idHelper = idMasterActor + HELPER_NAME;
Actor actor = new Actor(idHelper, masterBehaviour);

actor.setMaxMailboxSize(masterMaxMessages);
// helper is killing itself once it's finishing the task, but if it's master, it can not do that
// even if he had finished his job.
actor.isMaster = false;
actorPool.put(idHelper, actor);
return true;
```
## Behaviour  
General interface which will be used for creating new Behaviours. It contains only 2 methods onReceive which defines what the actor should do with the received message and oneException - which will usually be used to throw an exception if for example an actor is dead or to inform about the death of the actor. All actor's behaviours must implement these methods and define their own logic. This conforms to SOLID's second principle (Open closed principle) which is closed for modification but open for extension, examples of behaviours will be the next thing I will be explained in the implementation section.    
## Implementation
In order to read the streams I made a separate class called _SSEClient_ which is responsible for reading any stream of data it will get. It doesn't have something complicated just 3 fields and 3 methods for processing the incoming data. Such as checking if it's end of line, making a connection via _HttpURLConnection_ instance and _InputStream_ instance. Passing the _InputStream_ to the method of readEvent() which performs multiple checks for better reading the data.  

The next, more interesting part is inside the behaviour. I created a separate package(or library or behaviours) where I have put 3 classes which implements the Behaviour interface, all of them are responsible only for specific tasks, thanks to that, when creating an actor - he knows what to do with the message he has got.  

First behaviour I added here was the _SSEClientBehaviour_ it is responsible for actor's sse stream handling and passing to next actor for stream processing, so what does it mean. First of all this class overrides the method _onRecieve()_ & _onException()_ on exception roughly just kills an actor if an exception is thrown or error of some sort occurred. But _onReceive()_ has the logic itself for defining what the actor should do with the message it gets so inside we can find that it sets the URL for the stream(which is passed in form of a message to the actor) then create an instance of a _SSECLlient_ discussed above, and gets data. Also, here is the "message-panic" check is performed, so in case it encounters this message, the actor will kill itself and print that actor died. But if there is a valid message then it creates a new actor with _JsonBehavior_ which will process the streams for first actor. For every stream of handling tweets (SSE connection) we have an actor which is responsible for receiving tweets. After receiving tweet, if there is no actor for handling JSON tweet incoming for this stream, SSE actor creates JSON handler actor. This approach is great in a situation when scaling of listening streams is required and system must deal with them. It also evades bottle-neck, when several SSE streams are bombing only one JSON actor with messages. Even considering that actors can call for helpers that will take part of load it is problematic - creation of helper requires time and all messages now must be redirected to the helper, which is also requiring small amount of time that can become a catastrophically long, if there is high load incoming every moment of time and each millisecond is on count.

_JSONBehaviour_ - because the streams are in a json format it would be clever to parse and extract the data we need from them in a clever but simple way. Because later on we need to do some manipulations with that data, and at least make it more human-readable. So an _ObjectMapper_ first is created and then first check is performed, like if the data is not null then extract the required fields. Next first line is reading generally all the data in stream(it was used initially to check if it does its job normally and to see what fields we have there), then we extract the "tweet" from the all incoming data via this lines of code:  
```java
// extract tweet and save to variable
JsonNode tweetNode = jsonNode.get("message").get("tweet").get("text");
tweet = tweetNode.asText() + " ";
```
The same is done for extracting and saving to a variable the user name. Finally, printing the data we are interested in. Which contains the user, the tweet, score of the words found in the tweet. _EmotionHandler_ is responsible for emotion analysis. Here I initialized a map for storing the key-value pairs from the emotions route, where key is emotion and value is the number of points assigned to that emotion. First what was done is putting this information into the HashMap and the problem I encountered was that there are not only words but phrases too(e.g. not good, not bad, right direction...)  and I had to take this into account when putting these into the map. So in the method _onReceive()_ I took the following steps(each of them is commented for ease of understanding below):  
```java
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
```
Mapping was only the first step, second was to look inside the tweet and find the words which can be found in the newly created map and add the points for the matching words. Example: "I had a *good* day, I am so *happy* to be home. Here the matching words are happy and good, happy has 3 points and good has 3 points so in total we need to have 6 points for this tweet. I created a method called _getEmotionScore()_ which calculates the score adding up occurrences of words in the tweet from the emotionsMap, for every word/phrase from emotionMap the following is done: a temporary variable is initialized and stores the tweet to lower case in order to not omit the cases when we have GOOD and good(in the map everything is lower-case) if the tweet contains the emotion word from the map then the total score is equal to number of word appearances from emotionsMap multiplied by score number for that word.  
```java
score += amountOfEmotionWordAppearancesInTweet(tweet, emotionWord) * emotionsMap.get(emotionWord);
```
Here we can see the call of the method _amountOfEmotionWordAppearancesInTweet()_ with a self-explanatory name, it searches if the tweet contains a word from the map by processing the tweet by indexes and calling the methods of checking bounds because we can have in tweets spaces, punctuations, hashtags. You will ask why I have 2 methods which looks kind of the same - so, one is performing the check if the word is at the beggining of the tweet and the other one is performing the check if the word is anywhere else. Now we can get back to the method from which we started the discussion:  
```java
String reviewableFragment = "";
int counter = 0;

for (int startIndex = 0; startIndex < tweet.length() - emotionWord.length(); startIndex++) {
    int endingIndex = startIndex + emotionWord.length();
    if (startIndex != 0 && endingIndex != tweet.length()){
        if (verifyWordBounds(tweet.charAt(startIndex - 1), tweet.charAt(endingIndex))){
            reviewableFragment = tweet.substring(startIndex, endingIndex);
            System.out.println("HERE " + reviewableFragment + " | " + emotionWord);
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
```
Also for fun kind of I added a check to the score in the _getEmotionScore_ which through some if elses will output a message depending on how good or bad the user score is, for example if the score is 0 - the user is neutral, if above 7 then he is very happy and so on.  
## Technologies
Java 11 and Maven for Jackson  

## Status
Project Status _is_finished_
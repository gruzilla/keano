package space.decarbnow.collector.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.decarbnow.collector.api.InvalidPoiTypeException;
import space.decarbnow.collector.entities.MapPoi;
import space.decarbnow.collector.api.TwitterStatus;
import space.decarbnow.collector.rest.PoiRepository;
import space.decarbnow.collector.util.Converter;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/12/19.
 */
@Component
public class TwitterBean implements StatusListener, ConnectionLifeCycleListener {

    private static final Logger logger = LoggerFactory.getLogger(TwitterBean.class);
    private final PoiRepository repository;
    private TwitterStream twitterStream;
    private final Twitter twitter;
    private LocalDateTime lastTime;
    private String status;

    @Autowired
    public TwitterBean(PoiRepository repository) {

        logger.info("STARTING TWITTER BEAN....");

        this.repository = repository;
        twitter = new TwitterFactory(getTwitterConfig()).getInstance();

        try {
            twitterStream = new TwitterStreamFactory(getTwitterConfig()).getInstance();
        } catch (Exception e) {
            logger.error("ERROR: could not create twitter stream!");
            e.printStackTrace();
            return;
        }

        /*
        try {
            logger.info("importing tweets that can be retrieved..");
            runImport();
        } catch (Exception e) {
            logger.error("ERROR: could not import existing tweets!");
            e.printStackTrace();
        }
        */

        try {
            logger.info("initializing stream...");
            initializeTwitterStream();

        } catch (Exception e) {
            logger.error("ERROR: could not initialize listener!");
            e.printStackTrace();
        }
    }

    private Configuration getTwitterConfig() {
        //return TwitterFactory.getSingleton();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(System.getProperty("twitter4j.oauth.consumerKey"))
                .setOAuthConsumerSecret(System.getProperty("twitter4j.oauth.consumerSecret"))
                .setOAuthAccessToken(System.getProperty("twitter4j.oauth.accessToken"))
                .setOAuthAccessTokenSecret(System.getProperty("twitter4j.oauth.accessTokenSecret"))
                .setTweetModeExtended(true)
                .set
        ;
        return cb.build();
    }

    private void runImport() throws TwitterException {
        Query query = new Query("#decarbnow");
        QueryResult res;
        do {
            res = twitter.search(query);
            // logger.debug("we found " + res.getCount() + " tweets");
            Stream<MapPoi> pois = res.getTweets().stream().map(Converter::mapPoiFromStatus);
            pois.forEach(mapPoi -> {
                linkPreviousTweet(mapPoi);
                repository.save(mapPoi);

            }); // this.repository::save);
            logger.info("saved all retrievable tweets (" + pois.count() + ").");
            status = "import done";
        } while ((query = res.nextQuery()) != null);
    }

    private void linkPreviousTweet(MapPoi mapPoi) {
        if (mapPoi.getInReplyToTweetId() != null) {
            MapPoi previousTweet = repository.findByTweetId(mapPoi.getInReplyToTweetId());
            if (previousTweet != null) {
                previousTweet.setNextTweetId(mapPoi.getTweetId());
                repository.save(previousTweet);
            }
        }
    }

    private void initializeTwitterStream() {
        /*
        twitterStream.setOAuthAccessToken(new AccessToken(
            System.getProperty("twitter4j.oauth.accessToken"),
            System.getProperty("twitter4j.oauth.accessTokenSecret")
        ));
        */
        twitterStream.addListener(this);
        twitterStream.addConnectionLifeCycleListener(this);

        logger.info("starting listening to #decarbnow...");
        twitterStream.filter("#decarbnow");
        lastTime = LocalDateTime.now();
        status = "initialization done";
    }

    public TwitterStatus getStatus() {
        TwitterStatus ts = new TwitterStatus();

        ts.setLastStatusDateTime(lastTime);
        ts.setStatus(status);

        return ts;
    }

    @Override
    public void onException(Exception e) {
        logger.error("TWITTER STREAM ERROR: " + e, e);
        lastTime = LocalDateTime.now();
        status = "EXCEPTION: " + e;
    }

    @Override
    public void onStatus(Status status) {
        this.lastTime = LocalDateTime.now();
        this.status = "new status received";
        logger.debug("TWITTER STREAM: NEW TWEET");
        try {
            MapPoi newPoi = Converter.mapPoiFromStatus(status);
            linkPreviousTweet(newPoi);
            repository.save(newPoi);
            logger.debug("TWITTER STREAM: DONE.");
            this.lastTime = LocalDateTime.now();
            this.status = "new status saved " + status.getId() + " - " + status.getText();
        } catch (InvalidPoiTypeException e) {
            logger.debug("TWITTER STREAM: ERROR, INVALID POI TYPE");
            this.lastTime = LocalDateTime.now();
            this.status = "ERROR: " + e.getMessage();
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        logger.debug("TWITTER STREAM WARNING: Deletion Notice: " + statusDeletionNotice);
        lastTime = LocalDateTime.now();
        status = "Deletion Notice";
    }

    @Override
    public void onTrackLimitationNotice(int i) {
        logger.debug("TWITTER STREAM WARNING: Track Limitation Notice: " + i);
        lastTime = LocalDateTime.now();
        status = "Limitation Notice: " + i;
    }

    @Override
    public void onScrubGeo(long l, long l1) {
        logger.debug("TWITTER STREAM WARNING: Scrub Geo: " + l + " " + l1);
    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {
        lastTime = LocalDateTime.now();
        status = "Stall Warning!";
        logger.debug("TWITTER STREAM WARNING: Stalling: " + stallWarning);
    }

    @Override
    public void onConnect() {
        lastTime = LocalDateTime.now();
        status = "Stream Connected and waiting for Tweets";
        logger.debug("TWITTER STREAM CONNECTED.");
    }

    @Override
    public void onDisconnect() {
        lastTime = LocalDateTime.now();
        status = "Stream Disconnected";
        logger.debug("TWITTER STREAM DISCONNECTED.");
    }

    @Override
    public void onCleanUp() {
    }
}

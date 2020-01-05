package space.decarbnow.collector.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.decarbnow.collector.entities.MapPoi;
import space.decarbnow.collector.pojos.TwitterStatus;
import space.decarbnow.collector.rest.PoiRepository;
import space.decarbnow.collector.util.Converter;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/12/19.
 */
@Component
public class TwitterBean implements StatusListener {

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
        this.twitter = TwitterFactory.getSingleton();
        twitter.setOAuthAccessToken(new AccessToken(
                System.getProperty("twitter4j.oauth.accessToken"),
                System.getProperty("twitter4j.oauth.accessTokenSecret")
        ));

        try {
            this.twitterStream = new TwitterStreamFactory().getInstance();
        } catch (Exception e) {
            logger.error("ERROR: could not create twitter stream!");
            e.printStackTrace();
            return;
        }
        twitter.onRateLimitReached(rateLimitStatusEvent -> {
            if (rateLimitStatusEvent.isAccountRateLimitStatus()) {
                status = "Rate Limit Exeeded " + rateLimitStatusEvent.getRateLimitStatus().getLimit() + "/" + rateLimitStatusEvent.getRateLimitStatus().getRemaining()
            }
        });

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

    private void runImport() throws TwitterException {
        Query query = new Query("#decarbnow");
        QueryResult res;
        do {
            res = twitter.search(query);
            // logger.debug("we found " + res.getCount() + " tweets");
            Stream<MapPoi> pois = res.getTweets().stream().map(Converter::mapPoiFromStatus);
            pois.forEach(this.repository::save);
            logger.info("saved all retrievable tweets (" + pois.count() + ").");
            status = "import done";
        } while ((query = res.nextQuery()) != null);
    }

    private void initializeTwitterStream() {
        /*
        twitterStream.setOAuthAccessToken(new AccessToken(
            System.getProperty("twitter4j.oauth.accessToken"),
            System.getProperty("twitter4j.oauth.accessTokenSecret")
        ));
        */
        twitterStream.addListener(this);

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
        MapPoi lastPoi = Converter.mapPoiFromStatus(status);
        repository.save(lastPoi);
        logger.debug("TWITTER STREAM: DONE.");
        this.lastTime = LocalDateTime.now();
        this.status = "new status saved " + status.getId() + " - " + status.getText();
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
}

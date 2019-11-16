package space.decarbnow.collector.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.decarbnow.collector.entities.MapPoi;
import space.decarbnow.collector.rest.PoiRepository;
import space.decarbnow.collector.util.Converter;
import twitter4j.*;
import twitter4j.auth.AccessToken;

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

    @Autowired
    public TwitterBean(PoiRepository repository) {
        this.repository = repository;
        logger.debug("STARTING TWITTER BEAN....");


        try {
            Twitter twitter = TwitterFactory.getSingleton();
            twitter.setOAuthAccessToken(new AccessToken(
                System.getProperty("twitter4j.oauth.accessToken"),
                System.getProperty("twitter4j.oauth.accessTokenSecret")
            ));

            logger.debug("importing tweets that can be retrieved..");
            Query query = new Query("#decarbnow");
            QueryResult res;
            do {
                res = twitter.search(query);
                // logger.debug("we found " + res.getCount() + " tweets");
                Stream<MapPoi> pois = res.getTweets().stream().map(Converter::mapPoiFromStatus);
                pois.forEach(this.repository::save);
                logger.debug("saved all retrievable tweets.");
            } while ((query = res.nextQuery()) != null);

            logger.debug("creating listening...");

            TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
            twitterStream.setOAuthAccessToken(new AccessToken(
                System.getProperty("twitter4j.oauth.accessToken"),
                System.getProperty("twitter4j.oauth.accessTokenSecret")
            ));
            twitterStream.addListener(this);


            logger.debug("starting listening...");
            twitterStream.filter("#decarbnow");

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onException(Exception e) {
        logger.error("TWITTER STREAM ERROR: " + e, e);
    }

    @Override
    public void onStatus(Status status) {
        logger.debug("TWITTER STREAM: NEW TWEET");
        repository.save(Converter.mapPoiFromStatus(status));
        logger.debug("TWITTER STREAM: DONE.");
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        logger.debug("TWITTER STREAM WARNING: Deletion Notice: " + statusDeletionNotice);
    }

    @Override
    public void onTrackLimitationNotice(int i) {
        logger.debug("TWITTER STREAM WARNING: Track Limitation Notice: " + i);
    }

    @Override
    public void onScrubGeo(long l, long l1) {
        logger.debug("TWITTER STREAM WARNING: Scrub Geo: " + l + " " + l1);
    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {
        logger.debug("TWITTER STREAM WARNING: Stalling: " + stallWarning);
    }
}

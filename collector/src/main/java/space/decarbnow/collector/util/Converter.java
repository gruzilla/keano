package space.decarbnow.collector.util;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.decarbnow.collector.api.InvalidGeoHashException;
import space.decarbnow.collector.entities.MapPoi;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/12/19.
 */
public abstract class Converter {
    public static final int MIN_GEOHASH_LENGTH = 9;
    private static Logger logger = LoggerFactory.getLogger(MapPoi.class);
    private final static PrecisionModel precisionModel = new PrecisionModel();
    private final static int SRID = 4326;
    private final static GeometryFactory geometryFactory = new GeometryFactory(precisionModel, SRID);

    public static Point createPoint(double lng, double lat) {
        CoordinateSequence cs = CoordinateArraySequenceFactory.instance().create(new Coordinate[]{new Coordinate(lng, lat)});
        return new Point(cs, new GeometryFactory(precisionModel, SRID));
    }

    public static Point createPoint(String geohash) throws InvalidGeoHashException {
        if (geohash.length() < MIN_GEOHASH_LENGTH || geohash.indexOf('a') >= 0 || geohash.indexOf('i') >= 0 || geohash.indexOf('l') >= 0 || geohash.indexOf('o') >= 0) {
            throw new InvalidGeoHashException();
        }
        org.locationtech.spatial4j.shape.Point position = GeohashUtils.decode(geohash, SpatialContext.GEO).getCenter();
        if (position == null) {
            throw new InvalidGeoHashException();
        };
        return createPoint(position.getX(), position.getY());
    }

    public static MapPoi mapPoiFromStatus(Status status) {
        MapPoi p = new MapPoi();
        String t = status.getText();
        p.setTweetId(status.getId());

        List<String> validTypes = new ArrayList<>();
        validTypes.add("transition");
        validTypes.add("climateaction");
        validTypes.add("pollution");

        logger.info("TWEET: " + t + "\n");

        // set Type and Position by taking the first valid type and the first hashtag that converts to a geohash
        for (HashtagEntity hte : status.getHashtagEntities()) {
            String hashTagText = hte.getText();
            if (p.getType() == null && validTypes.contains(hashTagText)) {
                p.setType(hashTagText);
            }

            if (p.getPosition() == null) {
                try {
                    Point position = createPoint(hashTagText);
                    logger.info("-> Position from " + hashTagText + ": " + position.getX() + " " + position.getY());
                    p.setPosition(position);
                } catch (InvalidGeoHashException ignored) {
                }
            }
        }

        // set OriginalUrl by combining the users ScreenName and the tweets id using twitters url-schema
        p.setUrlOriginalTweet("https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId());
        logger.info("-> OriginalUrl: " + p.getUrlOriginalTweet());

        // set Text by using the status' full text
        p.setText(status.getText());
        logger.info("-> Text: " + p.getText());

        // convert status' created at
        p.setCreatedAt(LocalDateTime.ofInstant(status.getCreatedAt().toInstant(), ZoneId.systemDefault()));

        // set InReplyUrl by combining the ScreenName of the replying user and the status-id using twitters url-schema
        if (status.getInReplyToScreenName() != null) {
            origUrl = "https://twitter.com/" + status.getInReplyToScreenName() + "/status/" + status.getInReplyToStatusId();
            p.setInReplyToTweetId(status.getInReplyToStatusId());
            p.setUrlInReplyTweet("https://twitter.com/" + status.getInReplyToScreenName() + "/status/" + status.getInReplyToStatusId());
            logger.info("-> InReplyUrl: " + p.getUrlInReplyTweet());
        }

        // set QuotedUrl by combining the quoted users ScreenName and the tweets id using the twitter url-schema
        if (status.getQuotedStatusPermalink() != null) {
            p.setUrlQuotedTweet("https://twitter.com/" + status.getQuotedStatus().getUser().getScreenName() + "/status/" + status.getQuotedStatusId());
            logger.info("-> QuotedUrl: " + p.getUrlQuotedTweet());
        }

        return p;
    }
}

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
    public static final int MIN_GEOHASH_LENGTH = 6;
    private static Logger logger = LoggerFactory.getLogger(MapPoi.class);
    private final static PrecisionModel precisionModel = new PrecisionModel();
    private final static int SRID = 4326;
    private final static GeometryFactory geometryFactory = new GeometryFactory(precisionModel, SRID);

    public static Point createPoint(double lng, double lat) {
        CoordinateSequence cs = CoordinateArraySequenceFactory.instance().create(new Coordinate[]{new Coordinate(lng, lat)});
        return new Point(cs, new GeometryFactory(precisionModel, SRID));
    }

    public static Point createPoint(String geohash) throws InvalidGeoHashException {
        org.locationtech.spatial4j.shape.Point position = GeohashUtils.decode(geohash, SpatialContext.GEO).getCenter();
        if (position == null) {
            throw new InvalidGeoHashException();
        };
        return createPoint(position.getX(), position.getY());
    }

    public static MapPoi mapPoiFromStatus(Status status) {
        MapPoi p = new MapPoi();
        String t = status.getText();

        List<String> validTypes = new ArrayList<>();
        validTypes.add("transition");
        validTypes.add("climateaction");
        validTypes.add("pollution");

        logger.debug("TWEET: " + t);

        // set Type and Position by taking the first valid type and the first hashtag that converts to a geohash
        for (HashtagEntity hte : status.getHashtagEntities()) {
            String hashTagText = hte.getText();
            if (p.getType() == null && validTypes.contains(hashTagText)) {
                p.setType(hashTagText);
            }

            if (hashTagText.length() > MIN_GEOHASH_LENGTH && p.getPosition() == null) {
                try {
                    Point position = createPoint(hashTagText);
                    logger.debug("-> Position: " + position.getX() + " " + position.getY());
                    p.setPosition(position);
                } catch (InvalidGeoHashException ignored) {
                }
            }
        }

        // set OriginalUrl by combining the users url and the tweets id using twitters url-schema
        p.setUrlOriginalTweet(status.getUser().getURL() + "/status/" + status.getId());
        logger.debug("-> OriginalUrl: " + p.getUrlOriginalTweet());

        // set Text by using the status' full text
        p.setText(status.getText());
        logger.debug("-> Text: " + p.getText());

        // convert status' created at
        p.setCreatedAt(LocalDateTime.ofInstant(status.getCreatedAt().toInstant(), ZoneId.systemDefault()));

        // set InReplyUrl by combining the ScreenName of the replying user and the status-id using twitters url-schema
        if (status.getInReplyToScreenName() != null) {
            p.setUrlInReplyTweet("https://twitter.com/" + status.getInReplyToScreenName() + "/status/" + status.getInReplyToStatusId());
            logger.debug("-> InReplyUrl: " + p.getUrlInReplyTweet());
        }

        // set QuotedUrl by combining the quoted users url and the tweets id using the twitter url-schema
        if (status.getQuotedStatusPermalink() != null) {
            p.setUrlQuotedTweet(status.getQuotedStatus().getUser().getURL() + "/status/" + status.getQuotedStatusId());
            logger.debug("-> QuotedUrl: " + p.getUrlQuotedTweet());
        }

        return p;
    }
}

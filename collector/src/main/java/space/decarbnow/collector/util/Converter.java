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
import space.decarbnow.collector.entities.MapPoi;
import twitter4j.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/12/19.
 */
public abstract class Converter {
    private static Logger logger = LoggerFactory.getLogger(MapPoi.class);
    private final static PrecisionModel precisionModel = new PrecisionModel();
    private final static int SRID = 4326;
    private final static GeometryFactory geometryFactory = new GeometryFactory(precisionModel, SRID);

    public static Point createPoint(double lng, double lat) {
        CoordinateSequence cs = CoordinateArraySequenceFactory.instance().create(new Coordinate[]{new Coordinate(lng, lat)});
        return new Point(cs, new GeometryFactory(precisionModel, SRID));
    }

    public static Point createPoint(String geohash) {
        org.locationtech.spatial4j.shape.Point position = GeohashUtils.decode(geohash, SpatialContext.GEO).getCenter();
        return createPoint(position.getX(), position.getY());
    }

    public static MapPoi mapPoiFromStatus(Status status) {
        MapPoi p = new MapPoi();
        String t = status.getText();

        logger.debug("TWEET: " + t);

        Pattern r = Pattern.compile("#([a-zA-Z]+)\\s+#([a-zA-Z]+)\\s+@([a-zA-Z0-9]+)\\s+(.+)");
        Matcher m = r.matcher(t);

        String detectionTag = null;
        String layerTag = null;
        String geo = null;
        String text = null;
        String origUrl = null;

        if (m.matches()) {
            logger.debug("-> matches");
            detectionTag = m.group(1);
            layerTag = m.group(2);
            geo = m.group(3);
            text = m.group(4);
        }

        if (status.getInReplyToScreenName() != null) {
            origUrl = "https://twitter.com/" + status.getInReplyToScreenName() + "/status/" + status.getInReplyToStatusId();
        }

        if (status.getQuotedStatusPermalink() != null) {
            origUrl = status.getQuotedStatusPermalink().getURL();
        }

        if (geo != null) {
            Point position = createPoint(geo);
            logger.debug("-> Position: " + position.getX() + " " + position.getY());
            p.setPosition(position);
        }
        logger.debug("-> OrigUrl: " + origUrl);
        logger.debug("-> Text: " + text);
        logger.debug("-> Tag: " + layerTag);
        p.setType((layerTag != null) ? layerTag : "unkown");
        p.setMessage(text);
        p.setUrlLinkedTweet(origUrl);

        return p;
    }
}

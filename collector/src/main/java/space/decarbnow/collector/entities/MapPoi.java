package space.decarbnow.collector.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.decarbnow.collector.rest.JsonPointDeserializer;
import space.decarbnow.collector.rest.PointJsonSerializer;
import twitter4j.Status;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static space.decarbnow.collector.util.Converter.createPoint;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/7/19.
 */
@Entity
@Data
public class MapPoi {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    @JsonSerialize(using = PointJsonSerializer.class)
    @JsonDeserialize(using = JsonPointDeserializer.class)
    @Column(nullable = true, columnDefinition = "Geometry(Point,4326)")
    private Point position;

    @Column(nullable = true)
    private String urlQuotedTweet;

    @Column(nullable = true)
    private String urlInReplyTweet;

    @Column(nullable = false)
    private String type;

    @Column(nullable = true)
    private String message;

    @Column(nullable = true)
    private String urlOriginalTweet;
}

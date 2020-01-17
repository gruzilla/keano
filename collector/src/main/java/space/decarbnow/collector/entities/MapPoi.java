package space.decarbnow.collector.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.hibernate.annotations.*;
import org.locationtech.jts.geom.Point;
import space.decarbnow.collector.rest.JsonPointDeserializer;
import space.decarbnow.collector.rest.PointJsonSerializer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/7/19.
 */
@Entity
@Data
@Filters( {
    @Filter(name="filterHiddenTweets", condition=":hide IS FALSE")
} )
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
    private String text;

    @Column(nullable = true)
    private String urlOriginalTweet;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean hide = false;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private Long tweetId;

    @Column(nullable = true)
    private Long inReplyToTweetId;

    @Column(nullable = true)
    private Long nextTweetId;

    @Column(nullable = true)
    private boolean replyFromSameUser = false;
}

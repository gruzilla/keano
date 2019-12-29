package space.decarbnow.collector.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import space.decarbnow.collector.beans.TwitterBean;
import space.decarbnow.collector.entities.MapPoi;
import space.decarbnow.collector.entities.TwitterStatus;

import java.util.stream.Stream;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/12/19.
 */
@RestController
public class CustomEndpoints {

    private final PoiRepository repository;
    private final TwitterBean twitter;

    @Autowired
    public CustomEndpoints(PoiRepository repository, TwitterBean twitter) {
        this.repository = repository;
        this.twitter = twitter;
    }

    @GetMapping("/count")
    public long test() {
        Stream<Iterable<MapPoi>> pois = Stream.of(repository.findAll());
        return pois.count();
    }


    @GetMapping("/status")
    public TwitterStatus insert() {
        return twitter.getStatus();
    }

}

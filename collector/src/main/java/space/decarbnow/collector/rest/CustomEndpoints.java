package space.decarbnow.collector.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import space.decarbnow.collector.entities.MapPoi;

import java.util.stream.Stream;

import static space.decarbnow.collector.util.Converter.createPoint;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/12/19.
 */
@RestController
public class CustomEndpoints {

    private final PoiRepository repository;

    @Autowired
    public CustomEndpoints(PoiRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/test")
    public String test() {
        Stream<Iterable<MapPoi>> pois = Stream.of(repository.findAll());
        return "yey " + pois.count();
    }


    @GetMapping("/insert")
    public String insert() {
        MapPoi poi = new MapPoi();
        poi.setMessage("new");
        poi.setType("pollution");
        poi.setPosition(createPoint(48.1862, 16.3672));
        repository.save(poi);
        return "yey! saved!";
    }

}

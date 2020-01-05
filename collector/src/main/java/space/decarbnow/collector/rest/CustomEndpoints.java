package space.decarbnow.collector.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.decarbnow.collector.beans.TwitterBean;
import space.decarbnow.collector.pojos.TwitterStatus;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/12/19.
 */
@CrossOrigin(origins = "*")
@RestController()
@RequestMapping("api/status")
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
        return repository.count();
    }

    @GetMapping("/")
    public TwitterStatus insert() {
        return twitter.getStatus();
    }
}

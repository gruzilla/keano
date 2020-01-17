package space.decarbnow.collector.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import space.decarbnow.collector.beans.TwitterBean;
import space.decarbnow.collector.api.TwitterStatus;
import space.decarbnow.collector.entities.MapPoi;

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

    @GetMapping("/render/{tweetId}")
    public String renderSideBarElement(@PathVariable Long tweetId) {
        MapPoi status = repository.findByTweetId(tweetId);
        if (status == null) {
            return "";
        }

        String text = "<div id=\"tweet-" + status.getTweetId() + "\"></div>";
        if (status.isReplyFromSameUser() && status.getInReplyToTweetId() != null) {
            text += "<a class=\"nextTweet\" href=\"/api/rendered/" + status.getNextTweetId() + "></a>";
        }
        return text;
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

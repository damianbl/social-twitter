package eu.softelo.controllers;

import com.google.common.base.Optional;
import eu.softelo.infrastructure.caching.CacheKeys;
import eu.softelo.infrastructure.caching.CacheService;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.twitter.api.CursoredList;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by dabl on 2014-09-12.
 */
@Controller
@RequestMapping("/")
public class TwitterController {
    private Twitter twitter;

    private ConnectionRepository connectionRepository;

    private CacheService cacheService;

    @Inject
    public TwitterController(Twitter twitter, ConnectionRepository connectionRepository, CacheService cacheService) {
        this.cacheService = cacheService;
        this.twitter = twitter;
        this.connectionRepository = connectionRepository;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String index(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        } else {
            return "/connect/twitterConnected";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/followers")
    public String followers(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }

        Twitter twitterRest = new TwitterTemplate("PSsOLMfMcPmecFmSXKx3rq4y2", "hx9ZQUJ5yj3vdlMGI9BRfRnOpFRhh0CX9AD24KfNgGaM857Vgm");

        LinkedHashMap map = (LinkedHashMap) twitterRest.restOperations().getForObject(URI.create("https://api.twitter.com/1.1/application/rate_limit_status.json"), Object.class);

        Optional<Object> optionalFollowers = cacheService.get(CacheKeys.TWITTER_FOLLOWERS.name());
        List<TwitterProfile> allFollowers;

        if (optionalFollowers.isPresent()) {
            allFollowers = (List<TwitterProfile>) optionalFollowers.get();
        } else {
            allFollowers = new ArrayList<TwitterProfile>();
            CursoredList<TwitterProfile> followers = twitter.friendOperations().getFollowers();
            allFollowers.addAll(followers);
            while (followers.hasNext()) {
                followers = twitter.friendOperations().getFollowersInCursor(followers.getNextCursor());
                allFollowers.addAll(followers);
            }
            cacheService.put(CacheKeys.TWITTER_FOLLOWERS.name(), allFollowers);
        }

        model.addAttribute("followers", allFollowers);

        return "followers";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/friends")
    public String friends(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }

        Optional<Object> optionalFriends = cacheService.get(CacheKeys.TWITTER_FRIENDS.name());

        List<TwitterProfile> allFriends;

        if (optionalFriends.isPresent()) {
            allFriends = (List<TwitterProfile>) optionalFriends.get();
        } else {
            allFriends = new ArrayList<TwitterProfile>();
            CursoredList<TwitterProfile> friends = twitter.friendOperations().getFriends();
            allFriends.addAll(friends);
            while (friends.hasNext()) {
                friends = twitter.friendOperations().getFollowersInCursor(friends.getNextCursor());
                allFriends.addAll(friends);
            }
            cacheService.put(CacheKeys.TWITTER_FRIENDS.name(), allFriends);
        }

        model.addAttribute("friends", allFriends);

        return "friends";
    }
}

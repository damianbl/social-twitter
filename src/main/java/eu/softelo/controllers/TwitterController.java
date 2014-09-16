package eu.softelo.controllers;

import com.google.common.base.Optional;
import eu.softelo.infrastructure.caching.CacheKeys;
import eu.softelo.infrastructure.caching.CacheService;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.*;

/**
 * Created by dabl on 2014-09-12.
 */
@Controller
@RequestMapping("/")
public class TwitterController {
    @Value("${cacheing.enabled}")
    private Boolean cachingEnabled;

    @Value("${spring.social.twitter.resource.limits.url}")
    private String resourceLimitsUrl;

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
    public String index() {
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

        setLimits(TwitterResource.FOLLOWERS, TwitterOperation.LIST, model);

        Optional<Object> optionalFollowers = retrieve(CacheKeys.TWITTER_FOLLOWERS);

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

            saveIfNeeded(CacheKeys.TWITTER_FOLLOWERS, allFollowers);
        }

        model.addAttribute("followers", allFollowers);

        return "followers";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/friends")
    public String friends(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }

        setLimits(TwitterResource.FRIENDS, TwitterOperation.LIST, model);

        Optional<Object> optionalFriends = retrieve(CacheKeys.TWITTER_FRIENDS);

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
            saveIfNeeded(CacheKeys.TWITTER_FRIENDS, allFriends);
        }

        model.addAttribute("friends", allFriends);

        return "friends";
    }

    private Optional<Object> retrieve(CacheKeys key) {
        if (cachingEnabled) {
            return cacheService.get(key.name());
        } else {
            return Optional.absent();
        }
    }

    private void saveIfNeeded(CacheKeys key, List<TwitterProfile> allFollowers) {
        if (cachingEnabled) {
            cacheService.put(key.name(), allFollowers);
        }
    }

    private void setLimits(TwitterResource resource, TwitterOperation operation, Model model) {
        Map result = (Map) twitter.restOperations().getForObject(URI.create(resourceLimitsUrl + resource.name().toLowerCase()), Object.class);

        Map resources = (Map) result.get("resources");

        Map limits = (Map) ((Map) resources.get(resource.name().toLowerCase())).get("/" + resource.name().toLowerCase() + "/" + operation.name().toLowerCase());

        model.addAttribute("limit", limits.get("limit")).addAttribute("remaining", limits.get("remaining"));
    }
}
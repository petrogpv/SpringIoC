package ua.rd.services;

import ua.rd.domain.Tweet;

public interface TweetService {
    Iterable<Tweet> allTweets();
}

package ua.rd.repository;

import ua.rd.domain.Tweet;

public interface TweetRepository {

    Iterable<Tweet> allTweets();

}

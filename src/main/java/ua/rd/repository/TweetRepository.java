package ua.rd.repository;

import ua.rd.domain.Tweet;
import ua.rd.ioc.Benchmark;

public interface TweetRepository {

    Iterable<Tweet> allTweets();
    String methodToBenchmark(String string);
    String methodToBenchmark2(String string);

}

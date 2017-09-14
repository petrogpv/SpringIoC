package ua.rd.repository;

import ua.rd.domain.Tweet;
import ua.rd.ioc.Benchmark;

import java.util.Arrays;
import java.util.List;

public class InMemTweetRepository implements TweetRepository {

    private List<Tweet> tweets;
    {

    }
    public void init(){
        tweets = Arrays.asList(
                new Tweet(1L, "First Mesg", null),
                new Tweet(2L, "Second Mesg", null)
        );
    }
    @Benchmark(enabled = false)
    public String methodToBenchmark(String string){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new StringBuilder(string).reverse().toString();
    }
    @Benchmark
    public String methodToBenchmark2(String string){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new StringBuilder(string).reverse().toString();
    }

    @Override
    public Iterable<Tweet> allTweets() {
        return tweets;
    }
}

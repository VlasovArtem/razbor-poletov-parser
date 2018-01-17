package org.avlasov.razborpoletov.reader.entity.statistic;

/**
 * Created by artemvlasov on 25/06/2017.
 */
public class PodcastStatistic {

    private final long longestPodcast;
    private final long totalPodcastsTime;

    public PodcastStatistic(long longestPodcast, long totalPodcastsTime) {
        this.longestPodcast = longestPodcast;
        this.totalPodcastsTime = totalPodcastsTime;
    }

    public long getLongestPodcast() {
        return longestPodcast;
    }

    public long getTotalPodcastsTime() {
        return totalPodcastsTime;
    }
}

package org.avlasov.razborpoletov.reader.entity.statistic;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.avlasov.razborpoletov.reader.entity.User;

import java.util.List;

/**
 * Created by artemvlasov on 20/05/15.
 */
@JsonAutoDetect
public class ProjectStatistics {
    private int totalGuests;
    private List<User> top5Podcasters;
    private long longestPodcastMillis;
    private long totalPodcastsTimeMillis;
    private int maleGuests;
    private int femaleGuests;
    private List<String> programmingLanguages;
    private int totalAge;

    public int getTotalGuests() {
        return totalGuests;
    }

    public void setTotalGuests(int totalGuests) {
        this.totalGuests = totalGuests;
    }

    public List<User> getTop5Podcasters() {
        return top5Podcasters;
    }

    public void setTop5Podcasters(List<User> top5Podcasters) {
        this.top5Podcasters = top5Podcasters;
    }

    public long getLongestPodcastMillis() {
        return longestPodcastMillis;
    }

    public void setLongestPodcastMillis(long longestPodcastMillis) {
        this.longestPodcastMillis = longestPodcastMillis;
    }

    public long getTotalPodcastsTimeMillis() {
        return totalPodcastsTimeMillis;
    }

    public void setTotalPodcastsTimeMillis(long totalPodcastsTimeMillis) {
        this.totalPodcastsTimeMillis = totalPodcastsTimeMillis;
    }

    public int getMaleGuests() {
        return maleGuests;
    }

    public void setMaleGuests(int maleGuests) {
        this.maleGuests = maleGuests;
    }

    public int getFemaleGuests() {
        return femaleGuests;
    }

    public void setFemaleGuests(int femaleGuests) {
        this.femaleGuests = femaleGuests;
    }

    public List<String> getProgrammingLanguages() {
        return programmingLanguages;
    }

    public void setProgrammingLanguages(List<String> programmingLanguages) {
        this.programmingLanguages = programmingLanguages;
    }

    public int getTotalAge() {
        return totalAge;
    }

    public void setTotalAge(int totalAge) {
        this.totalAge = totalAge;
    }
}

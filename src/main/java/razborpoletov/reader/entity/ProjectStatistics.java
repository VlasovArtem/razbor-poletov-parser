package razborpoletov.reader.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

/**
 * Created by artemvlasov on 20/05/15.
 */
@JsonAutoDetect
public class ProjectStatistics {
    private int totalGuests;
    private List<Guest> top5Guests;
    private long longestPodcastMillis;
    private long totalPodcastsTimeMillis;
    private List<Guest> topNamedGuests;
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

    public List<Guest> getTop5Guests() {
        return top5Guests;
    }

    public void setTop5Guests(List<Guest> top5Guests) {
        this.top5Guests = top5Guests;
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

    public List<Guest> getTopNamedGuests() {
        return topNamedGuests;
    }

    public void setTopNamedGuests(List<Guest> topNamedGuests) {
        this.topNamedGuests = topNamedGuests;
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

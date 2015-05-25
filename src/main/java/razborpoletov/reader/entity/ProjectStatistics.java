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
    private long longestPodcast;
    private long totalPodcastsTime;
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

    public long getLongestPodcast() {
        return longestPodcast;
    }

    public void setLongestPodcast(long longestPodcast) {
        this.longestPodcast = longestPodcast;
    }

    public long getTotalPodcastsTime() {
        return totalPodcastsTime;
    }

    public void setTotalPodcastsTime(long totalPodcastsTime) {
        this.totalPodcastsTime = totalPodcastsTime;
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

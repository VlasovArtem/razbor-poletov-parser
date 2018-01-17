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

}

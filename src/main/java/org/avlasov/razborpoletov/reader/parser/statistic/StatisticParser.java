package org.avlasov.razborpoletov.reader.parser.statistic;

import org.avlasov.razborpoletov.reader.entity.statistic.ProjectStatistics;
import org.avlasov.razborpoletov.reader.entity.statistic.UserStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by artemvlasov on 20/05/15.
 */
@Component
public class StatisticParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(StatisticParser.class);
    private final UserDataParser userDataParser;
    private final PodcastStatisticParser podcastStatisticParser;
    private final ProjectStatistics projectStatistics;

    @Autowired
    public StatisticParser(UserDataParser userDataParser, PodcastStatisticParser podcastStatisticParser) {
        this.userDataParser = userDataParser;
        this.podcastStatisticParser = podcastStatisticParser;
        projectStatistics = new ProjectStatistics();
    }

    public ProjectStatistics parseProjectStatistics(List<File> files) throws IOException, URISyntaxException {
        parseGuestStatistics(files);
//        findPodcastData(files);
        return projectStatistics;
    }

    /**
     * Parse guest statistics for podcast file.
     *
     * @param files
     * @throws IOException
     */
    private void parseGuestStatistics(List<File> files) throws IOException {
        UserStatistic userStatistic = userDataParser.getUserStatistic(files);
    }

}

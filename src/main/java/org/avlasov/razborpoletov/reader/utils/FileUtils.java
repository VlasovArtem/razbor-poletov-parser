package org.avlasov.razborpoletov.reader.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.entity.info.Conference;
import org.avlasov.razborpoletov.reader.entity.info.UsefulThing;
import org.avlasov.razborpoletov.reader.entity.statistic.ProjectStatistics;
import org.avlasov.razborpoletov.reader.entity.statistic.UserStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by artemvlasov on 25/06/2017.
 */
@Component
public class FileUtils {

    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public FileUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


//    /**
//     * Save object to the provided file
//     *
//     * @param objects Map of the objects
//     */
//    public void save(Map<String, Optional> objects) {
//        for (String s : objects.keySet()) {
//            save(objects.get(s), s);
//        }
//    }
//
//    /**
//     * Save provided object to the file
//     *
//     * @param optional Object
//     * @param filename filename of the file into which object will be saved.
//     */
//    public void save(Optional<?> optional, String filename) {
//        if (!optional.isPresent() && filename == null) {
//            LOGGER.info(String.format("Persistence data or filename is empty (%s)", optional.get().getClass().getSimpleName()));
//            return;
//        }
//        File file = new File(filename);
//        try {
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            ObjectMapper mapper = new ObjectMapper();
//            if (optional.get() instanceof ProjectStatistics && getClass().getResourceAsStream(Constants.PROJECT_STATISTICS_FILE) !=
//                    null) {
//                ProjectStatistics projectStatistics = (ProjectStatistics) optional.get();
//                optional = Optional.of(updateProjectStatistics(projectStatistics));
//            }
//            mapper.writeValue(file, optional.get());
//        } catch (IOException e) {
//            LOGGER.warn(e.getMessage());
//            e.printStackTrace();
//        }
//    }

    public void saveUsefulThingsToFile(List<UsefulThing> usefulThings) throws IOException {
        if (usefulThings == null) {
            LOGGER.info("Persisted useful things list is empty");
            return;
        }
        File file = new File(Constants.USEFUL_THINGS_FILE);
        file.createNewFile();
        objectMapper.writeValue(file, usefulThings);
    }

    public void saveConferencesToFile(List<Conference> conferences) throws IOException {
        File conferenceComplete = new File(Constants.CONFERENCES_FILE);
        objectMapper.writeValue(conferenceComplete, conferences);
    }

    public void saveStatisticsToFile(ProjectStatistics source) throws IOException {
        if (source == null) {
            LOGGER.info("Persisted statistics list is empty");
            return;
        }
        File statistics = new File(Constants.PROJECT_STATISTICS_FILE);
        statistics.createNewFile();
        if (getClass().getResourceAsStream(Constants.PROJECT_STATISTICS_FILE) != null) {
            source = updateProjectStatistics(source);
        }
        objectMapper.writeValue(statistics, source);
    }

    public void saveTwitterCountToFile(List<User> twitterCount) {
        File file = new File("creator-and-guests.txt");
        file.delete();
        try {
            file.createNewFile();
            try {
                for (User user : twitterCount) {
                    org.apache.commons.io.FileUtils.write(file, user.toString(), Charset.defaultCharset(), true);
                }
                org.apache.commons.io.FileUtils.write(file, String.format("-----------------------------------------------------\nTotal users: %d",
                        twitterCount.size()), Charset.defaultCharset(), true);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public boolean saveUserStatisticToFile(UserStatistic userStatistic) {
        return false;
    }

    private ProjectStatistics updateProjectStatistics(ProjectStatistics source) throws IOException {
        ProjectStatistics target = objectMapper.readValue(getClass().getResourceAsStream(Constants.PROJECT_STATISTICS_FILE),
                ProjectStatistics.class);
        target.setFemaleGuests(target.getFemaleGuests() + source.getFemaleGuests());
        target.setMaleGuests(target.getMaleGuests() + source.getMaleGuests());
        target.setTotalAge(target.getTotalAge() + source.getTotalAge());
        target.setTotalPodcastsTimeMillis(target.getTotalPodcastsTimeMillis() + source.getTotalPodcastsTimeMillis());
        target.setLongestPodcastMillis(target.getLongestPodcastMillis() > source.getLongestPodcastMillis() ? target.getLongestPodcastMillis()
                : source.getLongestPodcastMillis());
        target.setTotalGuests(target.getTotalGuests() + source.getTotalGuests());
        target.getProgrammingLanguages().addAll(source.getProgrammingLanguages());
        return target;
    }

    public boolean saveTwitterCountInJsonToFile(List<User> users) {
        File file = new File("creators-and-guests.json");
        try {
            file.delete();
            file.createNewFile();
            objectMapper.writeValue(file, users);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

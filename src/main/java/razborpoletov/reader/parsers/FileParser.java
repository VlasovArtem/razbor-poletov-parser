package razborpoletov.reader.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import razborpoletov.reader.PropertiesSelector;
import razborpoletov.reader.entity.Conference;
import razborpoletov.reader.entity.ProjectStatistics;
import razborpoletov.reader.entity.Twitter;
import razborpoletov.reader.entity.UsefulThing;
import razborpoletov.reader.utils.PodcastFolderUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static razborpoletov.reader.utils.Constants.*;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class FileParser {
    private final static Logger LOG = LoggerFactory.getLogger(FileParser.class);
    private List<File> podcastsFiles;

    public FileParser(String postsFolder) throws IOException {
        podcastsFiles = PodcastFolderUtils.collectPostsFolderPath(postsFolder);
    }

    public List<File> getPodcastsFiles() {
        return podcastsFiles;
    }

    /**
     * Find last podcast file throw Runtime Exception if the file of the last podcast is not a asciidoc file (check
     * extension of the file)
     *
     * @return Last podcast file
     */
    public File getLastPodcastFile() {
        File file = podcastsFiles.get(podcastsFiles.size() - 1);
        String asciidocFilePattern = ".+(?=(a*(sc)?i*(doc)?d?))\\.a*(sc)?i*(doc)?d?";
        if (Pattern.matches(asciidocFilePattern, file.getName())) {
            return file;
        } else {
            LOG.warn("Last podcast file is not asciidoc format, file format: {}", FilenameUtils.getExtension(file
                    .getName()));
            throw new RuntimeException();
        }
    }

    /**
     * Find podcast by number of the episode.
     * @param number number of the episode
     * @return find file
     */
    public File getPodcastByNumber(int number) {
        RegexFileFilter filter = new RegexFileFilter(Pattern.compile(".+-" + number + "\\..+"));
        List<File> filteredFiles = FileFilterUtils.filterList(filter, podcastsFiles);
        if (!filteredFiles.isEmpty()) {
            return filteredFiles.get(0);
        }
        return null;
    }

    /**
     * Save object to the provided file
     * @param objects Map of the objects
     */
    public void save(Map<String, Optional> objects) {
        for (String s : objects.keySet()) {
            save(objects.get(s), s);
        }
    }

    /**
     * Save provided object to the file
     * @param optional Object
     * @param filename filename of the file into which object will be saved.
     */
    public void save(Optional<?> optional, String filename) {
        if (!optional.isPresent() && filename == null) {
            LOG.info(String.format("Persistence data or filename is empty (%s)", optional.get().getClass().getSimpleName()));
            return;
        }
        File file = new File(filename);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            ObjectMapper mapper = new ObjectMapper();
            if (optional.get() instanceof ProjectStatistics && getClass().getResourceAsStream(PROJECT_STATISTICS_FILE) !=
                    null) {
                ProjectStatistics projectStatistics = (ProjectStatistics) optional.get();
                optional = Optional.of(updateProjectStatistics(projectStatistics));
            }
            mapper.writeValue(file, optional.get());
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveUsefulThingsToFile(List<UsefulThing> usefulThings) throws IOException {
        if (usefulThings == null) {
            LOG.info("Persisted useful things list is empty");
            return;
        }
        File file = new File(USEFUL_THINGS_FILE);
        file.createNewFile();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, usefulThings);
    }

    public void saveConferencesToFile(List<Conference> conferences) throws IOException {
        if (conferences == null) {
            LOG.info("Persisted conferences list is empty");
            return;
        }
        File conferenceComplete = new File(CONFERENCES_FILE);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(conferenceComplete, conferences);
    }

    public void saveStatisticsToFile(ProjectStatistics source) throws IOException {
        if (source == null) {
            LOG.info("Persisted statistics list is empty");
            return;
        }
        File statistics = new File(PROJECT_STATISTICS_FILE);
        statistics.createNewFile();
        ObjectMapper mapper = new ObjectMapper();
        if (getClass().getResourceAsStream(PROJECT_STATISTICS_FILE) != null) {
            source = updateProjectStatistics(source);
        }
        mapper.writeValue(statistics, source);
    }

    public static void saveTwitterCountToFile(Map<Twitter, Integer> twitterCount) throws IOException {
        Map<Twitter, Integer> sortedCount = sortByComparator(twitterCount);
        try (FileOutputStream fos = new FileOutputStream(new File("creator-and-guests.txt"))) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Twitter, Integer> entry : sortedCount.entrySet()) {
                String line = String.format("Twitter: %s, Nickname: %s, count: %d\n", entry.getKey().getAccountUrl(),
                        entry.getKey().getAccount(), entry.getValue());
                fos.write(line.getBytes());
            }
            fos.write(String.format("Total users: %d", sortedCount.size()).getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static Map<Twitter, Integer> sortByComparator(Map<Twitter, Integer> unsortedMap) {
        List<Map.Entry<Twitter, Integer>> list =
                new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));
        Map<Twitter, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Twitter, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private ProjectStatistics updateProjectStatistics(ProjectStatistics source) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProjectStatistics target = mapper.readValue(getClass().getResourceAsStream(PROJECT_STATISTICS_FILE),
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
}

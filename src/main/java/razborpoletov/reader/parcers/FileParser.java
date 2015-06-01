package razborpoletov.reader.parcers;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static razborpoletov.reader.utils.Constants.*;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class FileParser {
    private PropertiesSelector propertiesSelector;
    private final static Logger LOG = LoggerFactory.getLogger(FileParser.class);
    private List<File> podcastsFiles;

    public FileParser(PropertiesSelector propertiesSelector) throws IOException {
        this.propertiesSelector = Preconditions.checkNotNull(propertiesSelector, "Properties selector cannot be null");
        podcastsFiles = Arrays.asList(Preconditions.checkNotNull(new File(propertiesSelector.getProperty
                        (PODCASTS_FOLDER_PROP_NAME)).listFiles(), "Folder file list is empty"))
                .stream()
                .filter(file -> Pattern.matches("20([0-9]{2}-){3}episode-[0-9].+", file.getName()))
                .collect(Collectors.toList());
    }

    public List<File> getPodcastsFiles() throws IOException, URISyntaxException {
        return podcastsFiles;
    }

    public List<File> getPodcastAsciidocFiles() {
        RegexFileFilter filter = new RegexFileFilter(Pattern.compile(".+(?=(a*(sc)?i*(doc)?d?))\\.a*(sc)?i*(doc)?d?"));
        return FileFilterUtils.filterList(filter, podcastsFiles);
    }
    public File getLastPodcastFile() throws IOException, URISyntaxException {
        File file = podcastsFiles.get(podcastsFiles.size() - 1);
        if(Pattern.matches(".+(?=(a*(sc)?i*(doc)?d?))\\.a*(sc)?i*(doc)?d?", file.getName())) {
            return file;
        } else {
            LOG.warn("Last podcast file is not asciidoc format, file format: {}", FilenameUtils.getExtension(file
                    .getName()));
            throw new RuntimeException();
        }
    }
    public void saveLast(List<UsefulThing> usefulThings, List<Conference> conferences, ProjectStatistics statistics)
            throws IOException {
        saveUsefulThingsToFile(usefulThings);
        saveConferencesToFile(conferences);
        saveStatisticsToFile(statistics);
    }
    public void saveUsefulThingsToFile(List<UsefulThing> usefulThings) throws IOException {
        if(usefulThings == null) {
            LOG.info("Persisted useful things list is empty");
            return;
        }
        File file = new File(USEFUL_THINGS_FILE);
        file.createNewFile();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, usefulThings);
    }
    public void saveConferencesToFile(List<Conference> conferences) throws IOException {
        if(conferences == null) {
            LOG.info("Persisted conferences list is empty");
            return;
        }
        File conferenceComplete = new File(CONFERENCES_FILE);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(conferenceComplete, conferences);
    }
    public void saveStatisticsToFile(ProjectStatistics source) throws IOException {
        if(source == null) {
            LOG.info("Persisted statistics list is empty");
            return;
        }
        File statistics = new File(PROJECT_STATISTICS_FILE);
        statistics.createNewFile();
        ObjectMapper mapper = new ObjectMapper();
        if(getClass().getResourceAsStream(PROJECT_STATISTICS_FILE) != null) {
            source = updateProjectStatistics(source);
        }
        mapper.writeValue(statistics, source);
    }
    public void saveAll(List<UsefulThing> usefulThings, List<Conference> conferences, ProjectStatistics statistics) throws IOException {
        saveConferencesToFile(conferences);
        saveStatisticsToFile(statistics);
        saveUsefulThingsToFile(usefulThings);
    }
    public void saveTwitterCountToFile(Map<Twitter, Integer> twitterCount) throws IOException {
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
    private Map<Twitter, Integer> sortByComparator(Map<Twitter, Integer> unsortedMap) {
        List<Map.Entry<Twitter, Integer>> list =
                new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));
        Map<Twitter, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Twitter, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    public static long getPodcastId(File file) {
        String podcastName = file.getName();
        String[] splited = podcastName.split("-");
        long podcastId = 0;
        for(int i = 0; i < splited.length; i++) {
            if(Objects.equals(splited[i], "episode")) {
                if(splited[i + 1].contains(".")) {
                    podcastId = Integer.valueOf(splited[i + 1].split("\\.")[0]);
                } else {
                    podcastId = Integer.valueOf(splited[i + 1]);
                }

            }
        }
        if(podcastId == 0) {
            LOG.info("Podcast id of file {} parsed incorrect", file.getName());
        }
        return podcastId;
    }
    public static LocalDate getPodcastDate(File file) {
        int year = getDataByPattern(file.getName(), Pattern.compile("201[0-9]"));
        int month = getDataByPattern(file.getName(), Pattern.compile("\b0\b|0[1-9]|1[0-2]"));
        int day = getDataByPattern(file.getName(), Pattern.compile("0[1-9]|[1-2][0-9]|3[0-1]"));
        if(year == 0 || month == 0 || day == 0) {
            LOG.info("File {} has no one of the data: year {}, month {}, day {}", file.getName(), year, month, day);
            return null;
        }
        return LocalDate.of(year, month, day);
    }
    private static int getDataByPattern(String filename, Pattern pattern) {
        List<String> splitedFileName = Arrays.asList(filename.split("-"));
        List<String> filtered = splitedFileName.stream().filter(data -> pattern.matcher(data).matches()).collect
                (Collectors.toList());
        if(filtered.isEmpty()) {
            return 0;
        } else {
            return Integer.valueOf(filtered.stream().findFirst().get());
        }
    }
    private ProjectStatistics updateProjectStatistics(ProjectStatistics source) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProjectStatistics target = mapper.readValue(getClass().getResourceAsStream(PROJECT_STATISTICS_FILE),
                ProjectStatistics.class);
        target.setFemaleGuests(target.getFemaleGuests() + source.getFemaleGuests());
        target.setMaleGuests(target.getMaleGuests() + source.getMaleGuests());
        target.setTotalAge(target.getTotalAge() + source.getTotalAge());
        target.setTotalPodcastsTime(target.getTotalPodcastsTime() + source.getTotalPodcastsTime());
        target.setLongestPodcast(target.getLongestPodcast() > source.getLongestPodcast() ? target.getLongestPodcast()
                : source.getLongestPodcast());
        target.setTotalGuests(target.getTotalGuests() + source.getTotalGuests());
        target.getProgrammingLanguages().addAll(source.getProgrammingLanguages());
        return target;
    }
}

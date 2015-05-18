package razborpoletov.reader.parcers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import razborpoletov.reader.PropertiesSelector;
import razborpoletov.reader.entity.Conference;
import razborpoletov.reader.entity.Podcast;
import razborpoletov.reader.entity.Twitter;
import razborpoletov.reader.entity.UsefulThing;
import razborpoletov.reader.utils.AsciidocUtils;
import razborpoletov.reader.utils.HtmlUtils;
import razborpoletov.reader.utils.MarkdownUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class FileParser {
    private final static String MARKDOWN_FORMAT = "markdown";
    private final static String ASCII_DOC = "adoc";
    private final static String HTML = "html";
    private final static String PODCAST_FILE = "podcast.json";
    private final static String CONFERENCES_FILE = "conferences.json";
    private final static String USEFULSTUFF_FILE = "useful-things.json";
    private final static String PODCAST_ASSOCIATIONS_FILE = "podcast-associations.json";
    private final static String PODCAST_COUNT_KEY = "podcast.count";
    private final static String LAST_PODCAST_KEY = "last.podcast";
    private final static String CONF_COMPLETE = "complete-conferences.json";
    private final static String USEFUL_COMPLETE = "complete-useful-things.json";
    private List<Podcast> podcasts;
    private List<UsefulThing> usefulThings;
    private List<Conference> conferences;
    private Map<Twitter, Integer> twitterCount;
    private Map<String, Integer> podcastAssociations;
    private MarkdownUtils markdownUtils;
    private AsciidocUtils asciidocUtils;
    private HtmlUtils htmlUtils;
    private PropertiesSelector propertiesSelector;
    private final static Logger LOG = LoggerFactory.getLogger(FileParser.class);
    private boolean test;
    private File[] podcastsFolderFileList;

    public FileParser(String podcastsFolder, PropertiesSelector propertiesSelector, boolean test) throws IOException {
        Preconditions.checkNotNull(podcastsFolder, "Podcast folder cannot be null");
        this.propertiesSelector = Preconditions.checkNotNull(propertiesSelector, "Properties selector cannot be null");
        LOG.info("Podcasts folder set to: {}", podcastsFolder);
        markdownUtils = new MarkdownUtils();
        asciidocUtils = new AsciidocUtils();
        htmlUtils = new HtmlUtils();
        twitterCount = new HashMap<>();
        podcasts = new ArrayList<>();
        usefulThings = new ArrayList<>();
        conferences = new ArrayList<>();
        podcastAssociations = new HashMap<>();
        this.test = test;
        podcastsFolderFileList = Preconditions.checkNotNull(new File(podcastsFolder).listFiles(), "Folder file list is " +
                "empty");
    }

    @Deprecated
    /**
     * Parser for twitter associations for podcast to count most permanent guests
     */
    public void fileParserTwitter() throws IOException {
        for (File file : podcastsFolderFileList) {
            FilenameUtils.getExtension(file.getAbsolutePath());
            switch (FilenameUtils.getExtension(file.getAbsolutePath())) {
                case ASCII_DOC:
                    countTwitter(htmlUtils.parseTwitter(asciidocUtils.parseTwitterPart(file)));
                    break;
                case MARKDOWN_FORMAT:
                    countTwitter(htmlUtils.parseTwitter(Jsoup.parse(markdownUtils.parseToHtml(file))));
                    break;
                case HTML:
                    countTwitter(htmlUtils.parseTwitter(file));
                    break;
            }
        }
        persistTwitterCountToFile();
    }

    @Deprecated
    /**
     * Initial podcast parser, was created to parse all content form previous podcasts. No longer used.
     */
    public void fileParserPodcast() throws IOException, URISyntaxException {
        File[] files;
        if (test) {
            files = podcastsFolderFileList;
        } else {
            RegexFileFilter filter = new RegexFileFilter(Pattern.compile(".+(?=(a*(sc)?i*(doc)?d?))\\.a*(sc)?i*(doc)?d?"));
            files = FileFilterUtils.filter(filter, podcastsFolderFileList);
        }
        for (File file : files) {
            if(Pattern.matches("20([0-9]{2}-){3}episode-[0-9].+", file.getName())) {
                parseFile(file);
            }
        }
        persistPodcastsToFile(false);
        persistToFile(PODCAST_ASSOCIATIONS_FILE, false);
        propertiesSelector.setProperty(PODCAST_COUNT_KEY, files.length);
        propertiesSelector.setProperty(LAST_PODCAST_KEY, files[files.length - 1].getName());
    }

    public void fileParserLastPodcast() throws IOException, URISyntaxException {
        if(Integer.valueOf(propertiesSelector.getProperty(PODCAST_COUNT_KEY)) == podcastsFolderFileList.length &&
                Objects.equals(propertiesSelector.getProperty(LAST_PODCAST_KEY), podcastsFolderFileList[podcastsFolderFileList
                        .length - 1].getName())) {
            LOG.info("Podcasts is alread up to date. Last parsed podcast: {}", propertiesSelector.getProperty
                    ("last.podcast"));
        } else {
            File conferenceComplete = new File(CONF_COMPLETE);
            File usefulThingsComplete = new File(USEFUL_COMPLETE);
            ObjectMapper mapper = new ObjectMapper();
            List<Conference> conferencesTarget = mapper.readValue(conferenceComplete,
                    new TypeReference<List<Conference>>() {});
            List<UsefulThing> usefulThingsTarget = mapper.readValue(usefulThingsComplete,
                    new TypeReference<List<UsefulThing>>() {});
            File file = podcastsFolderFileList[podcastsFolderFileList.length - 1];
            if(Pattern.matches(".+(?=(a*(sc)?i*(doc)?d?))\\.a*(sc)?i*(doc)?d?", file.getName())) {
                parseFile(file);
                conferencesTarget.addAll(conferences);
                usefulThingsTarget.addAll(usefulThings);
                mapper.writeValue(conferenceComplete, conferencesTarget);
                mapper.writeValue(usefulThingsComplete, usefulThingsTarget);
            } else {
                LOG.warn("Last podcast file is not asciidoc format, file format: {}", FilenameUtils.getExtension(file
                        .getName()));
            }

        }
    }
    private void parseFile(File file) throws IOException, URISyntaxException {
        String podcastName = parsePodcastData(file);
        switch (FilenameUtils.getExtension(file.getName())) {
            case ASCII_DOC:
                parseUsefulStuffs(htmlUtils.parseUsefulStuffsAsciidoc(asciidocUtils.parsePartById(file, "_Полезняшки")),
                        podcastName);
                parseConferences(htmlUtils.parseConferencesAsciidoc(asciidocUtils.parsePartById(file, "_Конференции")),
                        podcastName);
                break;
            case MARKDOWN_FORMAT:
                parseUsefulStuffs(htmlUtils.parseUsefulStuff(markdownUtils.parseToHtml(file)), podcastName);
                break;
            case HTML:
                parseUsefulStuffs(htmlUtils.parseUsefulStuff(file), podcastName);
                break;
        }
    }
    private void persistTwitterCountToFile() throws IOException {
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
    private void persistPodcastsToFile(boolean update) {
        persistToFile(PODCAST_FILE, update);
        persistToFile(USEFULSTUFF_FILE, update);
        persistToFile(CONFERENCES_FILE, update);
    }
    private void persistToFile(String fileName, boolean update) {
        try(FileOutputStream fos = new FileOutputStream(new File(fileName), update)) {
            ObjectMapper objectMapper = new ObjectMapper();
            switch (fileName) {
                case PODCAST_FILE:
                    objectMapper.writeValue(fos, podcasts);
                    break;
                case USEFULSTUFF_FILE:
                    objectMapper.writeValue(fos, usefulThings);
                    break;
                case CONFERENCES_FILE:
                    objectMapper.writeValue(fos, conferences);
                    break;
                case PODCAST_ASSOCIATIONS_FILE:
                    objectMapper.writeValue(fos, podcastAssociations);
                    break;

            }
        } catch (IOException e) {
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

    private String parsePodcastData(File file) {
        Podcast podcast = new Podcast();
        String podcastName = file.getName();
        String[] splited = podcastName.split("-");

        podcast.setDate(LocalDateTime.of(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]),
                Integer.valueOf(splited[2]), 0, 0));
        int podcastId = 0;
        for(int i = 0; i < splited.length; i++) {
            if(Objects.equals(splited[i], "episode")) {
                if(splited[i + 1].contains(".")) {
                    podcastId = Integer.valueOf(splited[i + 1].split("\\.")[0]);
                } else {
                    podcastId = Integer.valueOf(splited[i + 1]);
                }

            }
        }
        podcast.setId(podcastId);
        podcasts.add(podcast);
        podcastAssociations.put(podcastName, podcasts.indexOf(podcast));
        return podcastName;
    }
    private void countTwitter(List<Twitter> twitters) {
        if(twitters != null) {
            for(Twitter twitter: twitters) {
                if (!twitterCount.containsKey(twitter)) {
                    twitterCount.put(twitter, 1);
                } else {
                    twitterCount.put(twitter, twitterCount.get(twitter) + 1);
                }
            }
        }
    }
    private void parseUsefulStuffs(List<UsefulThing> usefulStuffs, String podcastName) {
        if(usefulStuffs != null && usefulStuffs.size() != 0) {
            Podcast podcast = podcasts.get(podcastAssociations.get(podcastName));
            usefulStuffs.stream().forEach(us -> us.setPodcastId(podcast.getId()));
            this.usefulThings.addAll(usefulStuffs);
            podcast.setUsefulStuffs(usefulStuffs);
        }
    }
    private void parseConferences(List<Conference> conferences, String podcastName) {
        if(conferences != null && conferences.size() != 0) {
            this.conferences.addAll(conferences);
            podcasts.get(podcastAssociations.get(podcastName)).setConferences(conferences);
        }
    }
}

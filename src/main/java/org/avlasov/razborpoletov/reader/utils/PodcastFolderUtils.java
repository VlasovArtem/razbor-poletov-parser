package org.avlasov.razborpoletov.reader.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by artemvlasov on 28/12/15.
 */
@Component
public class PodcastFolderUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(PodcastFolderUtils.class);
    private static final String PODCAST_POST_FOLDER_APPENDER;
    private static String podcastFolderPattern;
    private static String podcastPostFilePattern = "20([0-9]{2}-){3}episode(-)?[0-9].+";
    private List<File> podcastsFiles;

    static {
        PODCAST_POST_FOLDER_APPENDER = "source" + File.separator + "_posts" + File.separator;
        podcastFolderPattern = ".+" + PODCAST_POST_FOLDER_APPENDER + "?";
    }

    @Autowired
    public PodcastFolderUtils(CommandLine commandLine) {
        podcastsFiles = collectFilesFromPodcastFolder(commandLine.getOptionValue(CommandLineArgument.GIT_FOLDER.getOption()));
    }

    public List<File> getAllPodcastFiles() {
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
            LOGGER.warn("Last podcast file is not asciidoc format, file format: {}",
                    FilenameUtils.getExtension(file.getName()));
            throw new RuntimeException();
        }
    }

    /**
     * Get Podcasts files by numbers 'from' to 'to'
     *
     * @param from from number
     * @param to   to number
     * @return filtered podcasts
     */
    public List<File> getPodcastsFiles(int from, int to) {
        if (from <= to) {
            return getPodcastFiles(IntStream.rangeClosed(from, to)
                    .toArray());
        }
        String message = String.format("'from' %d number cannot be greater, than 'to' %d number", from, to);
        LOGGER.error(message);
        throw new IllegalArgumentException(message);
    }

    /**
     * Get Podcast fileds by podcast numbers
     *
     * @param podcastNumbers Podcast numbers
     * @return files filtered by podcast numbers
     */
    public List<File> getPodcastFiles(int... podcastNumbers) {
        if (Objects.nonNull(podcastNumbers)) {
            return IntStream.of(podcastNumbers)
                    .distinct()
                    .mapToObj(this::getPodcastByNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Get Podcast Files filtered by filter
     *
     * @param filter Filter
     * @return Filtered podcasts files.
     */
    public List<File> getPodcastFiles(Predicate<File> filter) {
        return podcastsFiles
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Find podcast by number of the episode.
     *
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
     * Check Podcast folder and Podcast post folder is exist and contains information
     *
     * @param podcastFolderPath Podcast folder path provided by user of the API
     */
    public void checkPodcastFolder(String podcastFolderPath) {
        Objects.requireNonNull(podcastFolderPath, "Podcast folder path cannot be null");
        File podcastFolder = new File(podcastFolderPath);
        if (!podcastFolder.exists() || !podcastFolder.isDirectory()) {
            throw new RuntimeException("Folder with provided path is not exists or it is not directory");
        }
    }

    /**
     * Collect list of files from podcast folder, that matches by provided file pattern. Sorted by Podcast id;
     *
     * @param podcastFolderPath Podcast folder path, this path could match folder of the posts or path of the base folder
     *                          podcast path.
     * @return list of the file
     */
    private List<File> collectFilesFromPodcastFolder(String podcastFolderPath) {
        checkPodcastFolder(podcastFolderPath);
        File postsFolder;
        if (podcastFolderPath.matches(podcastFolderPattern)) {
            postsFolder = new File(podcastFolderPath);
        } else {
            postsFolder = new File(getPodcastPostsFolder(podcastFolderPath));
        }
        File[] postsFiles = postsFolder.listFiles();
        if (Objects.nonNull(postsFiles) && postsFiles.length > 0)
            return Arrays.stream(postsFiles)
                    .filter(file -> Pattern.matches(podcastPostFilePattern, file.getName()))
                    .sorted(Comparator.comparingInt(file -> PodcastFileUtils.getPodcastNumber(file).orElse(-999)))
                    .collect(Collectors.toList());
        else
            throw new RuntimeException("Podcast post folder is empty");
    }

    /**
     * Return podcast post folder
     *
     * @param podcastFolderPath Podcast folder path provided by user of the API
     * @return string
     */
    private String getPodcastPostsFolder(String podcastFolderPath) {
        return StringUtils.endsWith(podcastFolderPath, "/") ?
                podcastFolderPath + PODCAST_POST_FOLDER_APPENDER :
                podcastFolderPath + "/" + PODCAST_POST_FOLDER_APPENDER;
    }

}

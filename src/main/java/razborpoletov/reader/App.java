package razborpoletov.reader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import razborpoletov.reader.git.GitPuller;
import razborpoletov.reader.parsers.ConferenceParser;
import razborpoletov.reader.parsers.FileParser;
import razborpoletov.reader.parsers.StatisticParser;
import razborpoletov.reader.parsers.UsefulThingParser;
import razborpoletov.reader.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static razborpoletov.reader.utils.PodcastFolderUtils.*;

class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static final int MIN_SIZE_OF_A_PODCAST_NUMBER_SIZE = 2;
    private static FileParser fileParser;
    private static ConferenceParser conferenceParser;
    private static StatisticParser statisticParser;
    private static UsefulThingParser usefulThingParser;
    private static CommandLine commandLine;

    public static void main(String[] args ) throws IOException, URISyntaxException, GitAPIException {
        commandLine = checkCommandLineAttrs(args);
        if(!commandLine.hasOption("p")) {
            throw new RuntimeException("Podcast folder is not exists");
        }
        String podcastFolder = commandLine.getOptionValue("p");
        checkPodcastFolder(podcastFolder);
        fileParser = new FileParser(podcastFolder);
        conferenceParser = new ConferenceParser();
        statisticParser = new StatisticParser();
        usefulThingParser = new UsefulThingParser();
        if(commandLine.hasOption("a")) {
            saveAll();
        } else if(commandLine.hasOption("l")) {
            saveLast();
        } else if(commandLine.hasOption("n")) {
            parseEpisodeByNumber(Integer.parseInt(commandLine.getOptionValue("n")));
        } else if(commandLine.hasOption("b")) {
            String commandLineValue = commandLine.getOptionValue("b");
            Pattern pattern = Pattern.compile("[\\d]+");
            if(commandLineValue.matches("(\\d+,?\\s?)+") || commandLineValue.matches("\\d+\\s?-\\s?\\d+")) {
                Matcher matcher = pattern.matcher(commandLineValue);
                List<String> podcastNumbers = new ArrayList<>();
                while (matcher.find()) {
                    podcastNumbers.add(matcher.group());
                }
                if(commandLineValue.matches("\\d+\\s?-\\s?\\d+")) {
                    if(podcastNumbers.size() != 0 && podcastNumbers.size() == MIN_SIZE_OF_A_PODCAST_NUMBER_SIZE) {
                        int firstPodcast = Integer.valueOf(podcastNumbers.get(0));
                        int lastPodcast = Integer.valueOf(podcastNumbers.get(1));
                        if(firstPodcast > lastPodcast) {
                            LOG.warn("First podcast number cannot be greater than last podcast");
                        } else {
                            podcastNumbers = new ArrayList<>(lastPodcast-firstPodcast);
                            for(int i = firstPodcast; i <= lastPodcast; i++) {
                                podcastNumbers.add(String.valueOf(i));
                            }
                        }
                    } else {
                        LOG.warn("Batch of podcats numbers in format from - to, cannot contains more that two podcats" +
                                " numbers");
                    }
                }
                if(podcastNumbers.size() != 0) {
                    parseEpisodeByNumbers(podcastNumbers);
                } else {
                    LOG.error("Command line args contains invalid data " + commandLineValue);
                }
            }
        }
    }

    private static CommandLine checkCommandLineAttrs(String[] args) {
        CLIUtils cliUtils = new CLIUtils();
        if(args == null || args.length < 1) {
            cliUtils.printHelp();
            System.exit(0);
        }
        cliUtils.setArgs(args);
        CommandLine commandLine = null;
        try {
            commandLine = cliUtils.createCommandLine();
        } catch (ParseException e) {
            System.out.println("Incorrect argument");
            cliUtils.printHelp();
            System.exit(0);
        }
        if(commandLine.hasOption("h")){
            cliUtils.printHelp();
            System.exit(0);
        }
        if(!commandLine.hasOption("p")) {
            LOG.warn("Properties file path is empty");
            System.exit(0);
        }
        if(!new File(commandLine.getOptionValue("p")).exists()) {
            LOG.warn("Properties file is not exists");
            cliUtils.printHelp();
            System.exit(0);
        }
        return commandLine;
    }

    private static void checkGitPull(String podcastFolderPath) throws IOException, GitAPIException {
        GitPuller gitPuller = new GitPuller(podcastFolderPath);
        PullResult pullResult = gitPuller.pull();
        if(!pullResult.isSuccessful()) {
            throw new RuntimeException("Git pull execute unsuccessfully");
        } else if(pullResult.toString().contains("Already up-to-date.") || pullResult.toString().contains
                ("Already-up-to-date.")) {
            if(!commandLine.hasOption("i")) {
                LOG.info("All podcasts are parsed");
                System.exit(0);
            }
        }
    }

    /**
     * Save all podcasts from Razbor Poletov repository files.
     * @throws IOException if files is not exists
     * @throws URISyntaxException if error in URI of the link
     */
    private static void saveAll() throws IOException, URISyntaxException {
        save(parser(Optional.ofNullable(fileParser.getPodcastsFiles())));
    }

    /**
     * Save last podcasts information to file
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void saveLast() throws IOException, URISyntaxException {
        save(parser(Optional.of(Collections.singletonList(fileParser.getLastPodcastFile()))));
    }

    /**
     * Save specific podcast data
     * @param number of the podcast
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void parseEpisodeByNumber(int number) throws IOException, URISyntaxException {
        if(number <= 0) {
            throw new RuntimeException("Number of podcast is less or equals to 0");
        }
        save(parser(Optional.of(Collections.singletonList(fileParser.getPodcastByNumber(number)))));
    }

    /**
     * Parse episodes by provided podcats numbers and save to the file.
     * @param podcastNumbers list of the podcasts numbers (number of the episodes)
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void parseEpisodeByNumbers(List<String> podcastNumbers) throws IOException, URISyntaxException {
        List<File> files = podcastNumbers.stream()
                .map(podcastNumber -> fileParser.getPodcastByNumber(Integer.parseInt(podcastNumber)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if(files.size() != podcastNumbers.size()) {
            LOG.info("We cannot find several files with provided podcast numbers");
        }
        save(parser(Optional.of(files)));
    }

    /**
     * Parse episodes from provided list of the files.
     * @param files list of files
     * @return Map that contains name of the file, that will contains parsed content and list of parsed objects.
     * @throws IOException
     * @throws URISyntaxException
     */
    private static Map<String, Optional> parser(Optional<List<File>> files) throws IOException, URISyntaxException {
        Map<String, Optional> map = new HashMap<>();
        if(files.get().isEmpty()) {
            throw new RuntimeException("We cannot find file with provided information");
        }
        if(commandLine.hasOption("l") || commandLine.hasOption("a")) {
            map.put(Constants.CONFERENCES_FILE, Optional.ofNullable(conferenceParser.parse(files.get(), false)));
            map.put(Constants.PROJECT_STATISTICS_FILE, Optional.ofNullable(statisticParser.parseProjectStatistics(files.get())));
            map.put(Constants.USEFUL_THINGS_FILE, Optional.ofNullable(usefulThingParser.parse(files.get(), false)));
        } else {
            for (Option option : commandLine.getOptions()) {
                if (option.getOpt().equals("c")) {
                    map.put(Constants.CONFERENCES_FILE, Optional.ofNullable(conferenceParser.parse(files.get(), false)));
                } else if (option.getOpt().equals("s")) {
                    map.put(Constants.PROJECT_STATISTICS_FILE, Optional.ofNullable(statisticParser.parseProjectStatistics
                            (files.get())));
                } else if (option.getOpt().equals("u")) {
                    System.out.println(files.get());
                    map.put(Constants.USEFUL_THINGS_FILE, Optional.ofNullable(usefulThingParser.parse(files.get(), false)));
                }
            }
        }
        return map;

    }

    private static void save(Map<String, Optional> data) throws IOException, URISyntaxException {
        fileParser.save(data);
    }
}

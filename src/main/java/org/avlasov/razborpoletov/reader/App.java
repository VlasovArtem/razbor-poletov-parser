package org.avlasov.razborpoletov.reader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.config.AppConfig;
import org.avlasov.razborpoletov.reader.entity.info.Conference;
import org.avlasov.razborpoletov.reader.entity.info.UsefulThing;
import org.avlasov.razborpoletov.reader.entity.statistic.ProjectStatistics;
import org.avlasov.razborpoletov.reader.entity.statistic.UserStatistic;
import org.avlasov.razborpoletov.reader.enums.CreatorsGuestsArg;
import org.avlasov.razborpoletov.reader.git.GitPuller;
import org.avlasov.razborpoletov.reader.parser.data.ConferenceParser;
import org.avlasov.razborpoletov.reader.parser.data.FileParser;
import org.avlasov.razborpoletov.reader.parser.data.UsefulThingParser;
import org.avlasov.razborpoletov.reader.parser.statistic.StatisticParser;
import org.avlasov.razborpoletov.reader.service.UserStatisticService;
import org.avlasov.razborpoletov.reader.utils.CLIUtils;
import org.avlasov.razborpoletov.reader.utils.FileUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private static final int MIN_SIZE_OF_A_PODCAST_NUMBER_SIZE = 2;
    private CommandLine commandLine;
    private final FileParser fileParser;
    private final AnnotationConfigApplicationContext applicationContext;
    private final FileUtils fileUtils;

    public App(AnnotationConfigApplicationContext applicationContext, String[] args) throws IOException, URISyntaxException {
        this.applicationContext = applicationContext;
        this.commandLine = getCommandLine(args);
        fileParser = applicationContext.getBean(FileParser.class, commandLine.getOptionValue("g"), applicationContext.getBean(PodcastFolderUtils.class));
        fileUtils = applicationContext.getBean(FileUtils.class);
    }

    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException {
        App app = new App(new AnnotationConfigApplicationContext(AppConfig.class), args);
        app.validateData();
        app.parseData();
    }

    private CommandLine getCommandLine(String[] args) {
        CLIUtils cliUtils = applicationContext.getBean(CLIUtils.class, new Object[]{args});
        try {
            commandLine = cliUtils.createCommandLine();
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Incorrect argument");
            cliUtils.printHelp();
            System.exit(0);
        }
        if (!new File(commandLine.getOptionValue("g")).exists()) {
            LOGGER.warn("Properties file is not exists");
            cliUtils.printHelp();
            System.exit(0);
        }
        try {
            CreatorsGuestsArg.valueOf(commandLine.getOptionValue("cg").toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.error("cg argument value is not valid");
            cliUtils.printHelp();
            System.exit(0);
        }
        return commandLine;
    }

    private void validateData() throws IOException, GitAPIException {
        PodcastFolderUtils podcastFolderUtils = applicationContext.getBean(PodcastFolderUtils.class);
        podcastFolderUtils.checkPodcastFolder(commandLine.getOptionValue("g"));
        checkGitPull(commandLine.getOptionValue("g"));
    }

    private void checkGitPull(String podcastFolderPath) throws IOException, GitAPIException {
        GitPuller gitPuller = new GitPuller(podcastFolderPath);
        PullResult pullResult = gitPuller.pull();
        if (!pullResult.isSuccessful()) {
            throw new RuntimeException("Git pull execute unsuccessfully");
        } else if (pullResult.toString().contains("Already up-to-date.") || pullResult.toString().contains
                ("Already-up-to-date.")) {
            if (!commandLine.hasOption("i")) {
                LOGGER.info("All podcasts are parsed");
                System.exit(0);
            }
        }
    }

    private void parseData() throws IOException, URISyntaxException {
        if (commandLine.hasOption("a")) {
            saveAll();
        } else if (commandLine.hasOption("l")) {
            saveLast();
        } else if (commandLine.hasOption("n")) {
            parseEpisodeByNumber(Integer.parseInt(commandLine.getOptionValue("n")));
        } else if (commandLine.hasOption("b")) {
            parseBatchEpisodes();
        } else {
            saveAll();
        }
        if (commandLine.hasOption("cg")) {
            parseCreatorsAndGuests();
        }
    }

    private void parseCreatorsAndGuests() {
        CreatorsGuestsArg cg = CreatorsGuestsArg.valueOf(commandLine.getOptionValue("cg").toUpperCase());
        UserStatisticService bean = applicationContext.getBean(UserStatisticService.class);
        UserStatistic userStatistic = bean.parseUserStatistic(fileParser.getPodcastsFiles());
        bean.saveJsonData(userStatistic.getAllUsers());
    }

    private void parseBatchEpisodes() throws IOException, URISyntaxException {
        String commandLineValue = commandLine.getOptionValue("b");
        Pattern pattern = Pattern.compile("[\\d]+");
        if (commandLineValue.matches("(\\d+,?\\s?)+") || commandLineValue.matches("\\d+\\s?-\\s?\\d+")) {
            Matcher matcher = pattern.matcher(commandLineValue);
            List<String> podcastNumbers = new ArrayList<>();
            while (matcher.find()) {
                podcastNumbers.add(matcher.group());
            }
            if (commandLineValue.matches("\\d+\\s?-\\s?\\d+")) {
                if (podcastNumbers.size() != 0 && podcastNumbers.size() == MIN_SIZE_OF_A_PODCAST_NUMBER_SIZE) {
                    int firstPodcast = Integer.valueOf(podcastNumbers.get(0));
                    int lastPodcast = Integer.valueOf(podcastNumbers.get(1));
                    if (firstPodcast > lastPodcast) {
                        LOGGER.warn("First podcast number cannot be greater than last podcast");
                    } else {
                        podcastNumbers = IntStream
                                .rangeClosed(firstPodcast, lastPodcast)
                                .boxed()
                                .map(String::valueOf)
                                .collect(Collectors.toList());

                    }
                } else {
                    LOGGER.warn("Batch of podcats numbers in format from - to, cannot contains more that two podcats numbers");
                }
            }
            if (podcastNumbers.size() != 0) {
                parseEpisodeByNumbers(podcastNumbers);
            } else {
                LOGGER.error("Command line args contains invalid data " + commandLineValue);
            }
        }
    }

    /**
     * Save all podcasts from Razbor Poletov repository files.
     *
     * @throws IOException        if files is not exists
     * @throws URISyntaxException if error in URI of the link
     */
    private void saveAll() throws IOException, URISyntaxException {
        parseAndSaveData(fileParser.getPodcastsFiles());
    }

    /**
     * Save last podcasts information to file
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    private void saveLast() throws IOException, URISyntaxException {
        parseAndSaveData(Collections.singletonList(fileParser.getLastPodcastFile()));
    }

    /**
     * Save specific podcast data
     *
     * @param number of the podcast
     * @throws IOException
     * @throws URISyntaxException
     */
    private void parseEpisodeByNumber(int number) throws IOException, URISyntaxException {
        if (number <= 0) {
            throw new RuntimeException("Number of podcast is less or equals to 0");
        }
        parseAndSaveData(Collections.singletonList(fileParser.getPodcastByNumber(number)));
    }

    /**
     * Parse episodes by provided podcats numbers and save to the file.
     *
     * @param podcastNumbers list of the podcasts numbers (number of the episodes)
     * @throws IOException
     * @throws URISyntaxException
     */
    private void parseEpisodeByNumbers(List<String> podcastNumbers) throws IOException, URISyntaxException {
        parseAndSaveData(podcastNumbers.stream()
                .map(podcastNumber -> {
                    File podcastByNumber = fileParser.getPodcastByNumber(Integer.parseInt(podcastNumber));
                    if (Objects.isNull(podcastByNumber))
                        LOGGER.error("File with podcast number {} is not found.", podcastNumber);
                    return podcastByNumber;
                })
                .collect(Collectors.toList()));
    }

    private List<Conference> parseConferences(List<File> files) throws IOException, URISyntaxException {
        ConferenceParser conferenceParser = applicationContext.getBean(ConferenceParser.class);
        return conferenceParser.parse(files, false);
    }

    private List<UsefulThing> parseUsefulThings(List<File> files) throws IOException, URISyntaxException {
        UsefulThingParser usefulThingParser = applicationContext.getBean(UsefulThingParser.class);
        return usefulThingParser.parse(files, false);
    }

    private ProjectStatistics parseProjectStatistics(List<File> files) throws IOException, URISyntaxException {
        StatisticParser statisticParser = applicationContext.getBean(StatisticParser.class);
        return statisticParser.parseProjectStatistics(files);
    }

    private void saveConferences(List<Conference> conferences) throws IOException {
        fileUtils.saveConferencesToFile(conferences);
    }

    private void saveUsefulThings(List<UsefulThing> usefulThings) throws IOException {
        fileUtils.saveUsefulThingsToFile(usefulThings);
    }

    private void saveProjectStatistics(ProjectStatistics projectStatistics) throws IOException {
        fileUtils.saveStatisticsToFile(projectStatistics);
    }

    private void saveAllData(List<Conference> conferences, List<UsefulThing> usefulThings, ProjectStatistics projectStatistics) throws IOException {
        saveConferences(conferences);
        saveUsefulThings(usefulThings);
        saveProjectStatistics(projectStatistics);
    }

    private void parseAndSaveData(List<File> files) throws IOException, URISyntaxException {
        if (commandLine.hasOption("l") || commandLine.hasOption("a")) {
            saveAllData(parseConferences(files), parseUsefulThings(files), parseProjectStatistics(files));
        } else {
            for (Option option : commandLine.getOptions()) {
                if (option.getOpt().equals("c")) {
                    saveConferences(parseConferences(files));
                } else if (option.getOpt().equals("s")) {
                    saveProjectStatistics(parseProjectStatistics(files));
                } else if (option.getOpt().equals("u")) {
                    saveUsefulThings(parseUsefulThings(files));
                }
            }
        }
    }

}

package razborpoletov.reader;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import razborpoletov.reader.git.GitPuller;
import razborpoletov.reader.parcers.ConferenceParser;
import razborpoletov.reader.parcers.FileParser;
import razborpoletov.reader.parcers.StatisticParser;
import razborpoletov.reader.parcers.UsefulThingParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static razborpoletov.reader.utils.Constants.*;

class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws IOException, URISyntaxException, GitAPIException {
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
        if(commandLine.hasOption("h")) {
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
        PropertiesSelector propertiesSelector = Preconditions.checkNotNull(
                checkPropertiesSelectorClass(commandLine.getOptionValue("p")),
                "Properties selector is null");
        checkPodcastFolder(propertiesSelector);
        checkGitPull(propertiesSelector);
        FileParser fileParser = new FileParser(propertiesSelector);
        ConferenceParser conferenceParser = new ConferenceParser();
        StatisticParser statisticParser = new StatisticParser();
        UsefulThingParser usefulThingParser = new UsefulThingParser();
        if(commandLine.hasOption("a")) {
            List<File> podcastFiles = fileParser.getPodcastsFiles();
            if(commandLine.hasOption("l")) {
                fileParser.saveLast(
                        usefulThingParser.parseUsefulThings(podcastFiles, false),
                        conferenceParser.parseConferences(podcastFiles, false),
                        statisticParser.parseProjectStatistics(podcastFiles));
            } else {
                fileParser.saveAll(
                        usefulThingParser.parseUsefulThings(podcastFiles, false),
                        conferenceParser.parseConferences(podcastFiles, false),
                        statisticParser.parseProjectStatistics(podcastFiles));
            }
        } else if(commandLine.hasOption("l")) {
            saveLast(fileParser, conferenceParser, statisticParser, usefulThingParser, commandLine);
        } else {
            save(fileParser, conferenceParser, statisticParser, usefulThingParser, commandLine);
        }
    }

    private static void checkGitPull(PropertiesSelector propertiesSelector) throws IOException, GitAPIException {
        GitPuller gitPuller = new GitPuller(propertiesSelector.getProperty(LOCAL_GIT_FOLDER_PROP_NAME));
        PullResult pullResult = gitPuller.pull();
        if(!pullResult.isSuccessful()) {
            throw new RuntimeException("Git pull execute unsuccessfully");
        } else if(pullResult.toString().contains("Already up-to-date.")) {
            LOG.info("All podcasts are parsed");
            System.exit(0);
        }
    }

    /**
     * Check podcast folder address and files list size.
     * @param propertiesSelector Use to get podcast folder from properties file
     */
    private static void checkPodcastFolder(PropertiesSelector propertiesSelector) {
        File file = Preconditions.checkNotNull(new File(propertiesSelector.getProperty(PODCASTS_FOLDER_PROP_NAME)),
                "File folder is null, please check address of podcast property " +
                        "folder, current folder" + propertiesSelector.getProperty(PODCASTS_FOLDER_PROP_NAME));
        File[] listFiles = Preconditions.checkNotNull(file.listFiles(), "Podcast folder files list in null");
        if(listFiles.length == 0) {
            LOG.info("Folder is empty");
            System.exit(0);
        }
    }

    private static PropertiesSelector checkPropertiesSelectorClass(String filePath) throws IOException {
        PropertiesSelector propertiesSelector = new PropertiesSelector(filePath);
        for(String prop : new String[] {PODCASTS_FOLDER_PROP_NAME, LOCAL_GIT_FOLDER_PROP_NAME}) {
            if(propertiesSelector.getProperty(prop) == null) {
                throw new RuntimeException("There is incorrect data in properties file. Preferable keys: " +
                        "podcasts.count, podcasts.folder, local.git.folder");
            }
        }
        return propertiesSelector;
    }

    private static boolean checkOptions(CommandLine commandLine, List<String> equals, List<String> notEquals) {
        if(equals == null && notEquals.stream().allMatch
                (commandLine::hasOption)) {
            return false;
        } else if(notEquals == null && equals.stream().allMatch(commandLine::hasOption)) {
            return true;
        }
        return false;
    }

    /**
     * Update files using different parsers according to command line arguments.
     * @param fileParser
     * @param conferenceParser
     * @param statisticParser
     * @param usefulThingParser
     * @param commandLine
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void saveLast(FileParser fileParser, ConferenceParser conferenceParser, StatisticParser
            statisticParser, UsefulThingParser usefulThingParser, CommandLine commandLine) throws IOException, URISyntaxException {
        if(!commandLine.hasOption("l")) {
            LOG.info("Command line not contains l argument please check code");
        } else {
            File lastPodcast = fileParser.getLastPodcastFile();
            if(checkOptions(commandLine, Arrays.asList("c", "s", "u"), null)) {
                fileParser.saveLast(
                        usefulThingParser.parse(lastPodcast),
                        conferenceParser.parse(lastPodcast),
                        statisticParser.parseProjectStatistics(Collections.singletonList(lastPodcast)));
            } else {
                for (Option option : commandLine.getOptions()) {
                    if (option.getOpt().equals("c")) {
                        fileParser.saveConferencesToFile(conferenceParser.parse(lastPodcast));
                    } else if (option.getOpt().equals("s")) {
                        fileParser.saveStatisticsToFile(statisticParser.parseProjectStatistics(Collections.singletonList(lastPodcast)));
                    } else if (option.getOpt().equals("u")) {
                        fileParser.saveUsefulThingsToFile(usefulThingParser.parse(lastPodcast));
                    }
                }
            }
        }
    }

    private static void save(FileParser fileParser, ConferenceParser conferenceParser, StatisticParser
            statisticParser, UsefulThingParser usefulThingParser, CommandLine commandLine) throws IOException, URISyntaxException {
        List<File> files = fileParser.getPodcastsFiles();
        if(checkOptions(commandLine, Arrays.asList("c", "s", "u"), null)) {
            fileParser.saveAll(
                    usefulThingParser.parseUsefulThings(files, false),
                    conferenceParser.parseConferences(files, false),
                    statisticParser.parseProjectStatistics(files));
        } else {
            for (Option option : commandLine.getOptions()) {
                if (option.getOpt().equals("c")) {
                    fileParser.saveConferencesToFile(conferenceParser.parseConferences(files, false));
                } else if (option.getOpt().equals("s")) {
                    fileParser.saveStatisticsToFile(statisticParser.parseProjectStatistics(files));
                } else if (option.getOpt().equals("u")) {
                    fileParser.saveUsefulThingsToFile(usefulThingParser.parseUsefulThings(files, false));
                }
            }
        }
    }
}

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
import razborpoletov.reader.parsers.ConferenceParser;
import razborpoletov.reader.parsers.FileParser;
import razborpoletov.reader.parsers.StatisticParser;
import razborpoletov.reader.parsers.UsefulThingParser;
import razborpoletov.reader.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static razborpoletov.reader.utils.Constants.LOCAL_GIT_FOLDER_PROP_NAME;
import static razborpoletov.reader.utils.Constants.PODCASTS_FOLDER_PROP_NAME;

class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static FileParser fileParser;
    private static ConferenceParser conferenceParser;
    private static StatisticParser statisticParser;
    private static UsefulThingParser usefulThingParser;
    private static CommandLine commandLine;

    public static void main( String[] args ) throws IOException, URISyntaxException, GitAPIException {
        commandLine = checkCommandLineAttrs(args);
        PropertiesSelector propertiesSelector = Preconditions.checkNotNull(
                checkPropertiesSelectorClass(commandLine.getOptionValue("p")),
                "Properties selector is null");
        checkPodcastFolder(propertiesSelector);
        checkGitPull(propertiesSelector);

        fileParser = new FileParser(propertiesSelector);
        conferenceParser = new ConferenceParser();
        statisticParser = new StatisticParser();
        usefulThingParser = new UsefulThingParser();

        if(commandLine.hasOption("a")) {
            saveAll();
        } else if(commandLine.hasOption("l")) {
            saveLast();
        } else if(commandLine.hasOption("n")) {
            saveByNumber(Integer.parseInt(commandLine.getOptionValue("n")));
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
    private static void checkGitPull(PropertiesSelector propertiesSelector) throws IOException, GitAPIException {
        GitPuller gitPuller = new GitPuller(propertiesSelector.getProperty(LOCAL_GIT_FOLDER_PROP_NAME));
        PullResult pullResult = gitPuller.pull();
        if(!pullResult.isSuccessful()) {
            throw new RuntimeException("Git pull execute unsuccessfully");
        } else if(pullResult.toString().contains("Already up-to-date.") || pullResult.toString().contains
                ("Already-up-to-date.")) {
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
    private static void saveAll() throws IOException, URISyntaxException {
        save(Optional.ofNullable(fileParser.getPodcastsFiles()));
    }
    private static void saveLast() throws IOException, URISyntaxException {
        save(Optional.ofNullable(Collections.singletonList(fileParser.getLastPodcastFile())));
    }
    private static void saveByNumber(int number) throws IOException, URISyntaxException {
        if(number <= 0) {
            throw new RuntimeException("Number of podcast is less or equals to 0");
        }
        save(Optional.ofNullable(Collections.singletonList(fileParser.getPodcastByNumber(number))));
    }
    private static void save(Optional<List<File>> files) throws IOException, URISyntaxException {
        Map<String, Optional> map = new HashMap<>();
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
                    map.put(Constants.USEFUL_THINGS_FILE, Optional.ofNullable(usefulThingParser.parse(files.get(), false)));
                }
            }
        }
        fileParser.save(map);
    }
}

package org.avlasov.razborpoletov.reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.cli.ParserCommandLine;
import org.avlasov.razborpoletov.reader.git.GitPuller;
import org.avlasov.razborpoletov.reader.service.data.ServiceData;
import org.avlasov.razborpoletov.reader.service.data.impl.LinkServiceDataImpl;
import org.avlasov.razborpoletov.reader.service.data.impl.UsefulThingsServiceDataImpl;
import org.avlasov.razborpoletov.reader.service.data.impl.UserServiceDataImpl;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument.*;

@SpringBootApplication
@EnableAutoConfiguration
public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private ParserCommandLine commandLine;
    private ApplicationContext applicationContext;
    private PodcastFolderUtils podcastFolderUtils;

    @Autowired
    public App(ParserCommandLine commandLine, ApplicationContext applicationContext, PodcastFolderUtils podcastFolderUtils) {
        this.commandLine = commandLine;
        this.applicationContext = applicationContext;
        this.podcastFolderUtils = podcastFolderUtils;
        podcastFolderUtils.checkPodcastFolder(commandLine.getOptionValue(GIT_FOLDER));
    }

    public static void main(String[] args) throws Exception {
        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder();
        ConfigurableApplicationContext applicationContext = springApplicationBuilder
                .sources(App.class)
                .profiles("commandLine")
                .run(args);
        App app = applicationContext.getBean(App.class);
        app.evaluateParser();
        applicationContext.close();
    }

    public void evaluateParser() throws Exception {
        createDataFolder();
        checkGitPull();
        parseData();
    }

    private void parseData() {
        proceedData(getPodcastFilesToParse());
    }

    private List<File> getPodcastFilesToParse() {
        if (commandLine.hasOption(LAST)) {
            return Collections.singletonList(podcastFolderUtils.getLastPodcastFile());
        } else if (commandLine.hasOption(NUMBER)) {
            return Collections.singletonList(podcastFolderUtils.getPodcastByNumber(Integer.parseInt(commandLine.getOptionValue(NUMBER))));
        } else if (commandLine.hasOption(BATCH)) {
            return podcastFolderUtils.getPodcastFiles(getBatchEpisodes());
        } else {
            return podcastFolderUtils.getAllPodcastFiles();
        }
    }

    private int[] getBatchEpisodes() {
        String commandLineValue = commandLine.getOptionValue(BATCH);
        Pattern pattern = Pattern.compile("[\\d]+");
        if (commandLineValue.matches("(\\d+,?\\s?)+") || commandLineValue.matches("\\d+\\s?-\\s?\\d+")) {
            Matcher matcher = pattern.matcher(commandLineValue);
            List<String> podcastNumbers = new ArrayList<>();
            while (matcher.find()) {
                podcastNumbers.add(matcher.group());
            }
            if (commandLineValue.matches("\\d+\\s?-\\s?\\d+")) {
                int firstPodcast = Integer.valueOf(podcastNumbers.get(0));
                int lastPodcast = Integer.valueOf(podcastNumbers.get(1));
                if (firstPodcast > lastPodcast) {
                    String message = "First podcast number cannot be greater than last podcast";
                    LOGGER.warn(message);
                    throw new RuntimeException(message);
                } else {
                    return IntStream
                            .rangeClosed(firstPodcast, lastPodcast)
                            .toArray();
                }
            }
        }
        return new int[]{};
    }

    private void proceedData(List<File> files) {
        List<Consumer<List<File>>> consumers = new ArrayList<>();
        boolean saveAll = commandLine.hasOption(LAST) || commandLine.hasOption(ALL);
        if (commandLine.hasOption(USEFUL) || saveAll) {
            consumers.add(proceedData(UsefulThingsServiceDataImpl.class));
        }
        if (commandLine.hasOption(LINKS) || saveAll) {
            consumers.add(proceedData(LinkServiceDataImpl.class));
        }
        if (commandLine.hasOption(CREATORS_GUESTS) || saveAll) {
            consumers.add(proceedData(UserServiceDataImpl.class));
        }
        consumers
                .forEach(consumer -> consumer.accept(files));
    }

    private <T, R extends ServiceData<T>> Consumer<List<File>> proceedData(Class<R> clazz) {
        return files -> {
            R bean = applicationContext.getBean(clazz);
            bean.saveJsonData(bean.parse(files));
        };
    }

    private void checkGitPull() throws IOException, GitAPIException {
        LOGGER.info("Start trying to pull new episodes from git folder {}.", commandLine.getOptionValue(GIT_FOLDER));
        GitPuller gitPuller = new GitPuller(commandLine.getOptionValue(GIT_FOLDER));
        PullResult pullResult = gitPuller.pull();
        if (!pullResult.isSuccessful()) {
            throw new RuntimeException("Git pull execute unsuccessfully");
        } else if (pullResult.toString().contains("up-to-date.")) {
            if (!commandLine.hasOption(IGNORE_GIT)) {
                String message = String.format("All podcasts was parsed. Please set option %s to ignore this validation", IGNORE_GIT.getOption());
                LOGGER.info(message);
                throw new RuntimeException(message);
            }
        }
    }

    private void createDataFolder() throws IOException {
        File dataFolder = new File("./data");
        if (!dataFolder.exists()) {
            dataFolder.createNewFile();
            LOGGER.info("Data folder was created. Folder path {}.", dataFolder.getAbsolutePath());
        }
    }

}

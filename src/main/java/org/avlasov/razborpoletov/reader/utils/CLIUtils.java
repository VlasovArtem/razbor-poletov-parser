package org.avlasov.razborpoletov.reader.utils;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.avlasov.razborpoletov.reader.enums.CreatorsGuestsArgument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by artemvlasov on 21/05/15.
 */
@Component
@Profile("commandLine")
public class CLIUtils {

    private final static Logger LOGGER = LogManager.getLogger(CLIUtils.class);
    private Options options;
    private ApplicationArguments arguments;

    @Autowired
    public CLIUtils(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ApplicationArguments arguments) {
        this.arguments = arguments;
        options = new Options();
        options.addOption(Option.builder("a").longOpt("all").desc("Parse all data from all podcast files").build());
        options.addOption(Option.builder("l").longOpt("last").desc("Parse all data from last podcast file").build());
        options.addOption(Option.builder("u").longOpt("useful").desc("Parse useful things data").build());
        options.addOption(Option.builder("c").longOpt("creators-guests").desc("Parse creators and guests from twitter accounts in podcast description, Possible values: all or update.").hasArg(true).numberOfArgs(1).build());
        options.addOption(Option.builder("k").longOpt("links").desc("Parse links of the archive podcasts. This argument contains one arguments append - update or not to update existing file. true or 1 otherwise false. Use with batch, number or last otherwise it will parse all data.").build());
        options.addOption(Option.builder("s").longOpt("statistic").desc("Parse statistic data").build());
        options.addOption(Option.builder("n").longOpt("number").desc("Parse specific podcast with set number").hasArg(true).numberOfArgs(1).build());
        options.addOption(Option.builder("b").longOpt("batch").desc("Specify list of podcast to parse, divided by comma or hypen. Example: 1,2 or 1-5").hasArg(true).build());
        options.addOption(Option.builder("i").longOpt("ignore-git").desc("Ignore git repository check on update").build());
        options.addOption(Option.builder("g").longOpt("git-folder").desc("Razbor Poletov local git folder").required(true).hasArg(true).build()); //Replace with github API
        options.addOption(Option.builder("h").longOpt("help").desc("Show help").build());
    }

    public CommandLine createCommandLine() {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, arguments.getSourceArgs());
            if (!new File(commandLine.getOptionValue("g")).exists()) {
                LOGGER.warn("Properties file is not exists");
                printHelp();
                System.exit(0);
            }
            if (commandLine.hasOption(CommandLineArgument.CREATORS_GUESTS.getOption())) {
                CreatorsGuestsArgument.valueOf(commandLine.getOptionValue(CommandLineArgument.CREATORS_GUESTS.getOption()).toUpperCase());
            }
            return commandLine;
        } catch (ParseException e) {
            LOGGER.error(e);
            printHelp();
            throw new RuntimeException(e);
        }
    }

    private void printHelp() {
        final String commandLineSyntax = "java -jar RazborPoletovReader-1.0-SNAPSHOT.jar";
        final PrintWriter writer = new PrintWriter(System.out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                80,
                commandLineSyntax,
                "Options",
                options,
                3,
                5,
                "If you put in one line arguments a and l, l argument will be ignored." +
                        "\nYou should always put a, l or n argument." +
                        "\nPlease use i if you want parse content if git repository is already up to date" +
                        "\n----HELP----",
                true);
        writer.flush();
    }
}

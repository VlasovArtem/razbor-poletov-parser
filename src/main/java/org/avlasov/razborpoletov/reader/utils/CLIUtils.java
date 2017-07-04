package org.avlasov.razborpoletov.reader.utils;

import org.apache.commons.cli.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

/**
 * Created by artemvlasov on 21/05/15.
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CLIUtils {
    private Options options;
    private String[] args;

    private CLIUtils() {
        options = new Options();
        options.addOption(Option.builder("a").longOpt("all").desc("Parse all data from all podcast files").build());
        options.addOption(Option.builder("l").longOpt("last").desc("Parse all data from last podcast file").build());
        options.addOption(Option.builder("c").longOpt("conferences").desc("Parse conference data").build());
        options.addOption(Option.builder("u").longOpt("useful").desc("Parse useful things data").build());
        options.addOption(Option.builder("cg").longOpt("creators-guests").desc("Parse creators and guests from twitter accounts in podcast description, Possible values: all or update.").hasArg(true).numberOfArgs(1).build());
        options.addOption(Option.builder("s").longOpt("statistic").desc("Parse statistic data").build());
        options.addOption(Option.builder("n").longOpt("number").desc("Parse specific podcast with set number").hasArg(true).numberOfArgs(1).build());
        options.addOption(Option.builder("b").longOpt("batch").desc("Specify list of podcast to parse, divided by comma or hypen. Example: 1,2 or 1-5").hasArg(true).build());
        options.addOption(Option.builder("i").longOpt("ignore-git").desc("Ignore git repository check on update").build());
        options.addOption(Option.builder("g").longOpt("git-folder").desc("Razbor Poletov local git folder").required(true).hasArg(true).build()); //Replace with github API
        options.addOption(Option.builder("h").longOpt("help").desc("Show help").build());
    }

    public CLIUtils(String[] args) {
        this();
        this.args = args;
        if(args == null || args.length < 1) {
            printHelp();
            System.exit(0);
        }
    }

    public CommandLine createCommandLine() throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }
    public void printHelp() {
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

package razborpoletov.reader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;

/**
 * Created by artemvlasov on 21/05/15.
 */
public class CLIUtils {
    private Options options;
    private String[] args;

    public CLIUtils() {
        options = new Options();
        options.addOption(new Option("a", "all", false, "Parse all data from all podcast files"));
        options.addOption(new Option("l", "last", false, "Parse all data from last podcast file"));
        options.addOption(new Option("c", "conf", false, "Parse conference data"));
        options.addOption(new Option("u", "useful", false, "Parse useful things data"));
        options.addOption(new Option("s", "stat", false, "Parse statistic data"));
        options.addOption(new Option("n", "num", true, "Parse specific podcast with set number"));
        options.addOption(new Option("p", "prop", true, "Properties file address. If you will not put custom file " +
                "address program will you default properties file"));
        options.addOption(new Option("h", "help", false, "Show help"));
    }

    public CLIUtils(String[] args) {
        this();
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
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
                "If you put in one line arguments a and l, l argument will be ignored.\nYou should " +
                        "always put a, l or n argument.\n----HELP----",
                true);
        writer.flush();
    }
}

package org.avlasov.razborpoletov.reader.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created By artemvlasov on 04/01/2018
 **/
@Component
@Profile("commandLine")
public class ParserCommandLine {

    private CommandLine commandLine;

    @Autowired
    public ParserCommandLine(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public String getOptionValue(CommandLineArgument argument) {
        return commandLine.getOptionValue(argument.getOption());
    }

    public boolean hasOption(CommandLineArgument argument) {
        return commandLine.hasOption(argument.getOption());
    }

    public Option[] getOptions() {
        return commandLine.getOptions();
    }
}

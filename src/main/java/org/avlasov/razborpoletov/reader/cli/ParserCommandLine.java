package org.avlasov.razborpoletov.reader.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created By artemvlasov on 04/01/2018
 **/
@Component
@Profile("commandLine")
public class ParserCommandLine {

    private CommandLine commandLine;
    private List<CommandLineArgument> commandLineArguments;

    @Autowired
    public ParserCommandLine(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CommandLine commandLine) {
        this.commandLine = commandLine;
        commandLineArguments = collectCommandLineArguments();
    }

    public Optional<Option> getOption(CommandLineArgument argument) {
        return Stream.of(commandLine.getOptions())
                .filter(option -> argument.getOption().equals(option.getOpt()))
                .findFirst();
    }

    public String getOptionValue(CommandLineArgument argument) {
        return commandLine.getOptionValue(argument.getOption());
    }

    public boolean hasOption(CommandLineArgument argument) {
        return commandLine.hasOption(argument.getOption());
    }

    public List<CommandLineArgument> getOptionsArguments() {
        return commandLineArguments;
    }

    private List<CommandLineArgument> collectCommandLineArguments() {
        return Stream.of(commandLine.getOptions())
                .map(option -> Stream.of(CommandLineArgument.values())
                        .filter(argument -> argument.getOption().equals(option.getOpt()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Option[] getOptions() {
        return commandLine.getOptions();
    }
}

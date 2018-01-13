package org.avlasov.razborpoletov.reader.cli.enums;

/**
 * Created By artemvlasov on 04/01/2018
 **/
public enum CommandLineArgument {

    ALL("a"), LAST("l"), CONFERENCES("c"), USEFUL("u"), CREATORS_GUESTS("cg"), LINKS("k"), STATISTIC("s"), NUMBER("n"), BATCH("b"), IGNORE_GIT("i"), GIT_FOLDER("g"), HELP("h");

    private String option;

    CommandLineArgument(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}

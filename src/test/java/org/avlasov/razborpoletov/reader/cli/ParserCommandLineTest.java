package org.avlasov.razborpoletov.reader.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.hamcrest.collection.IsArrayWithSize;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created By artemvlasov on 14/01/2018
 **/
@RunWith(MockitoJUnitRunner.class)
public class ParserCommandLineTest {

    @Mock
    private CommandLine commandLine;
    @InjectMocks
    private ParserCommandLine parserCommandLine;

    @Test
    public void getOptionValue_WithValid_ReturnStringValue() {
        when(commandLine.getOptionValue(Mockito.anyString())).thenReturn("Test");
        String optionValue = parserCommandLine.getOptionValue(CommandLineArgument.CREATORS_GUESTS);
        assertEquals("Test", optionValue );
    }

    @Test
    public void getOptionValue_WithNotExistingOption_ReturnNull() {
        when(commandLine.getOptionValue(Mockito.anyString())).thenReturn(null);
        String optionValue = parserCommandLine.getOptionValue(CommandLineArgument.CREATORS_GUESTS);
        assertNull(optionValue);
    }

    @Test
    public void hasOption_WithExistingOption_ReturnTrue() {
        when(commandLine.hasOption(Mockito.anyString())).thenReturn(true);
        assertTrue(parserCommandLine.hasOption(CommandLineArgument.CREATORS_GUESTS));
    }

    @Test
    public void hasOption_WithNotExistingOption_ReturnFalse() {
        when(commandLine.hasOption(Mockito.anyString())).thenReturn(false);
        assertFalse(parserCommandLine.hasOption(CommandLineArgument.CREATORS_GUESTS));
    }

    @Test
    public void getOptions_WithExistingOptions_ReturnArray() {
        when(commandLine.getOptions()).thenReturn(getOptions());
        Option[] options = parserCommandLine.getOptions();
        assertThat(options, IsArrayWithSize.arrayWithSize(1));
    }

    @Test
    public void getOptions_WithEmptyOptions_ReturnEmptyArray() {
        when(commandLine.getOptions()).thenReturn(new Option[]{});
        Option[] options = parserCommandLine.getOptions();
        assertThat(options, IsArrayWithSize.emptyArray());
    }

    private Option[] getOptions() {
        return new Option[]{ new Option("c", "test") };
    }

}
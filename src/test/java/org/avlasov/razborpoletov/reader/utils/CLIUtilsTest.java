package org.avlasov.razborpoletov.reader.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.ApplicationArguments;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.eq;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created By artemvlasov on 16/01/2018
 **/
@PrepareForTest({CLIUtils.class})
public class CLIUtilsTest extends PowerMockitoTestCase {

    @Mock
    private File fileMock;
    @Mock
    private ApplicationArguments arguments;
    @Mock
    private DefaultParser defaultParserMock;
    @Mock
    private CommandLine commandLine;
    @InjectMocks
    private CLIUtils cliUtils;


    @Before
    public void setUp() throws Exception {
        whenNew(DefaultParser.class).withAnyArguments().thenReturn(defaultParserMock);
        when(defaultParserMock.parse(any(Options.class), anyVararg())).thenReturn(commandLine);
        whenNew(File.class).withAnyArguments().thenReturn(fileMock);
        when(fileMock.exists()).thenReturn(true);
        when(commandLine.hasOption(eq(CommandLineArgument.CREATORS_GUESTS.getOption()))).thenReturn(true);
        when(commandLine.getOptionValue(eq(CommandLineArgument.CREATORS_GUESTS.getOption()))).thenReturn("UPDATE");
    }

    @Test
    public void createCommandLine_WithValidData_ReturnCommandLine() {
        assertNotNull(cliUtils.createCommandLine());
    }

    @Test(expected = RuntimeException.class)
    public void createCommandLine_WithNotExistingGitFolder_ThrownException() {
        when(fileMock.exists()).thenReturn(false);
        cliUtils.createCommandLine();
    }

    @Test(expected = RuntimeException.class)
    public void createCommandLine_WithParseThrowException_ThrownException() throws Exception {
        when(defaultParserMock.parse(any(Options.class), anyVararg())).thenThrow(new ParseException(""));
        cliUtils.createCommandLine();
    }

}
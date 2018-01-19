package org.avlasov.razborpoletov.reader;

import org.avlasov.razborpoletov.reader.cli.ParserCommandLine;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.avlasov.razborpoletov.reader.git.GitPuller;
import org.avlasov.razborpoletov.reader.service.data.ServiceData;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.Collections;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 19/01/2018
 **/
@PrepareForTest({ App.class })
public class AppTest extends PowerMockitoTestCase {

    @Mock
    private ParserCommandLine parserCommandLine;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private PodcastFolderUtils folderUtils;
    @Mock
    private ServiceData serviceData;
    @Mock
    private File file;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GitPuller gitPuller;
    @Mock
    private PullResult pullResult;
    @InjectMocks
    private App app;

    @Before
    public void setUp() throws Exception {
        doNothing().when(folderUtils).checkPodcastFolder(anyString());
        whenNew(File.class).withAnyArguments().thenReturn(file);
        whenNew(GitPuller.class).withAnyArguments().thenReturn(gitPuller);
        when(gitPuller.pull()).thenReturn(pullResult);
        when(pullResult.isSuccessful()).thenReturn(true);
        when(pullResult.toString()).thenReturn("test");
        when(applicationContext.getBean((Class<ServiceData>) any())).thenReturn(serviceData);
        when(serviceData.parse(anyListOf(File.class))).thenReturn(Collections.emptyList());
        when(serviceData.saveJsonData(anyList())).thenReturn(file);
    }

    @Test
    public void evaluateParser_parserLastPodcast() throws Exception {
        when(parserCommandLine.hasOption(Mockito.eq(CommandLineArgument.LAST))).thenReturn(true);
        when(folderUtils.getLastPodcastFile()).thenReturn(file);
        app.evaluateParser();
    }

    @Test(expected = RuntimeException.class)
    public void evaluateParser_OnUnsuccessfulGitPull_ThrownException() throws Exception {
        when(pullResult.isSuccessful()).thenReturn(false);
        app.evaluateParser();
    }

    @Test(expected = RuntimeException.class)
    public void evaluateParser_WithGitAlreadyUpToDate_ThrownException() throws Exception {
        when(pullResult.toString()).thenReturn("Already up-to-date.");
        app.evaluateParser();
    }

    @Test
    public void evaluateParser_WithGitAlreadyUpToDateAndIgnoreProperty_ThrownException() throws Exception {
        when(pullResult.toString()).thenReturn("Already up-to-date.");
        when(parserCommandLine.hasOption(Mockito.eq(CommandLineArgument.IGNORE_GIT))).thenReturn(true);
        when(parserCommandLine.hasOption(Mockito.eq(CommandLineArgument.LAST))).thenReturn(true);
        when(folderUtils.getLastPodcastFile()).thenReturn(file);
        app.evaluateParser();
    }

    @Test
    public void evaluateParser_WithEpisodeNumberPodcast() throws Exception {
        when(parserCommandLine.hasOption(Mockito.eq(CommandLineArgument.NUMBER))).thenReturn(true);
        when(parserCommandLine.getOptionValue(Mockito.eq(CommandLineArgument.NUMBER))).thenReturn("10");
        when(folderUtils.getPodcastByNumber(anyInt())).thenReturn(file);
        app.evaluateParser();
    }

    @Test
    public void evaluateParser_WithCommaSeparatedBatchEpisodeNumbersPodcast() throws Exception {
        when(parserCommandLine.hasOption(Mockito.eq(CommandLineArgument.BATCH))).thenReturn(true);
        when(parserCommandLine.getOptionValue(Mockito.eq(CommandLineArgument.BATCH))).thenReturn("10,20");
        when(folderUtils.getPodcastFiles(any(int[].class))).thenReturn(Collections.singletonList(file));
        app.evaluateParser();
    }

    @Test
    public void evaluateParser_WithDashSeparatedBatchEpisodeNumbersPodcast() throws Exception {
        when(parserCommandLine.hasOption(Mockito.eq(CommandLineArgument.BATCH))).thenReturn(true);
        when(parserCommandLine.getOptionValue(Mockito.eq(CommandLineArgument.BATCH))).thenReturn("10-20");
        when(folderUtils.getPodcastFiles(any(int[].class))).thenReturn(Collections.singletonList(file));
        app.evaluateParser();
    }

    @Test(expected = RuntimeException.class)
    public void evaluateParser_WithDashSeparatedBatchEpisodeNumbersPodcastAndLastPodcastLessThanFirstPodcast() throws Exception {
        when(parserCommandLine.hasOption(Mockito.eq(CommandLineArgument.BATCH))).thenReturn(true);
        when(parserCommandLine.getOptionValue(Mockito.eq(CommandLineArgument.BATCH))).thenReturn("20-10");
        app.evaluateParser();
    }

    @Test(expected = Exception.class)
    public void evaluateParser_GitPullThrowsException_ThrownException() throws Exception {
        when(gitPuller.pull()).thenThrow(new InvalidConfigurationException("Test"));
        app.evaluateParser();
    }

    @Test
    public void evaluateParser_ParseAllData() throws Exception {
        app.evaluateParser();
    }

    @Test
    public void main() throws Exception {
        SpringApplicationBuilder springApplicationBuilder = mock(SpringApplicationBuilder.class, Answers.RETURNS_DEEP_STUBS.get());
        ConfigurableApplicationContext configurableApplicationContext = mock(ConfigurableApplicationContext.class);
        whenNew(SpringApplicationBuilder.class).withNoArguments().thenReturn(springApplicationBuilder);
        when(springApplicationBuilder.sources(eq(App.class)).profiles(eq("commandLine")).run(any(String[].class))).thenReturn(configurableApplicationContext);
        when(configurableApplicationContext.getBean(eq(App.class))).thenReturn(app);
        doNothing().when(configurableApplicationContext).close();
        App.main(new String[]{"test"});
    }
}
package org.avlasov.razborpoletov.reader.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 16/01/2018
 **/
@PrepareForTest({PodcastFolderUtils.class, PodcastFileUtils.class, FileFilterUtils.class})
public class PodcastFolderUtilsTest extends PowerMockitoTestCase {

    @Mock
    private File fileMock;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private CommandLine commandLine;

    @Before
    public void setUp() throws Exception {
        mockStatic(PodcastFileUtils.class, FileFilterUtils.class);
        whenNew(File.class).withAnyArguments().thenReturn(fileMock);
        when(fileMock.exists()).thenReturn(true);
        when(fileMock.isDirectory()).thenReturn(true);
        when(fileMock.listFiles()).thenReturn(new File[]{fileMock});
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(Optional.of(9));
        when(fileMock.getName()).thenReturn("2012-01-18-episode-7.adoc");
        when(FileFilterUtils.filterList(any(RegexFileFilter.class), any(Iterable.class))).thenReturn(Collections.singletonList(fileMock));
    }

    @Test
    public void constructInstance() {
        new PodcastFolderUtils(commandLine);
    }

    @Test
    public void constructInstance_WithMatchesPodcastFolderPath() {
        when(commandLine.getOptionValue(anyString())).thenReturn("test/source/_posts");
        new PodcastFolderUtils(commandLine);
    }

    @Test(expected = RuntimeException.class)
    public void constructInstance_WithEmptyPodcastsFolder_ThrownException() {
        when(fileMock.listFiles()).thenReturn(null);
        new PodcastFolderUtils(commandLine);
    }

    @Test
    public void getAllPodcastFiles_WithValidData_ReturnFilesCollection() {
        PodcastFolderUtils folderUtils = new PodcastFolderUtils(commandLine);
        List<File> allPodcastFiles = folderUtils.getAllPodcastFiles();
        assertThat(allPodcastFiles, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void getLastPodcastFile_WithValidData_ReturnFile() {
        PodcastFolderUtils folderUtils = new PodcastFolderUtils(commandLine);
        File lastPodcastFile = folderUtils.getLastPodcastFile();
        assertNotNull(lastPodcastFile);
    }

    @Test(expected = RuntimeException.class)
    public void getLastPodcastFile_WithInvalidFileName_ThrownException() {
        when(fileMock.getName()).thenReturn("2012-01-18-episode-7.html");
        PodcastFolderUtils folderUtils = new PodcastFolderUtils(commandLine);
        folderUtils.getLastPodcastFile();
    }

    @Test
    public void getPodcastsFiles_WithValidData_ReturnCollection() {
        PodcastFolderUtils folderUtils = new PodcastFolderUtils(commandLine);
        List<File> podcastsFiles = folderUtils.getPodcastsFiles(9, 9);
        assertThat(podcastsFiles, IsCollectionWithSize.hasSize(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPodcastsFiles_WithInvalidFromAndToNumber_ThrownException() {
        PodcastFolderUtils folderUtils = new PodcastFolderUtils(commandLine);
        List<File> podcastsFiles = folderUtils.getPodcastsFiles(9, 1);
    }

    @Test
    public void getPodcastFiles_WithNullNumbers_ReturnEmptyCollection() {
        PodcastFolderUtils podcastFolderUtils = new PodcastFolderUtils(commandLine);
        List<File> podcastFiles = podcastFolderUtils.getPodcastFiles((int [])null);
        assertThat(podcastFiles, IsEmptyCollection.empty());
    }

    @Test
    public void getPodcastFiles_WithNumbers_ReturnCollection() {
        PodcastFolderUtils podcastFolderUtils = new PodcastFolderUtils(commandLine);
        List<File> podcastFiles = podcastFolderUtils.getPodcastFiles(9);
        assertThat(podcastFiles, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void getPodcastFiles_WithFilter_ReturnCollection() {
        PodcastFolderUtils podcastFolderUtils = new PodcastFolderUtils(commandLine);
        List<File> podcastFiles = podcastFolderUtils.getPodcastFiles((file) -> true);
        assertThat(podcastFiles, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void getPodcastByNumber_WithValidNumber_ReturnFile() {
        PodcastFolderUtils podcastFolderUtils = new PodcastFolderUtils(commandLine);
        File podcastByNumber = podcastFolderUtils.getPodcastByNumber(9);
        assertNotNull(podcastByNumber);
    }

    @Test
    public void getPodcastByNumber_WithEmptyFilteredFiles_ReturnNull() {
        PodcastFolderUtils podcastFolderUtils = new PodcastFolderUtils(commandLine);
        when(FileFilterUtils.filterList(any(RegexFileFilter.class), any(Iterable.class))).thenReturn(Collections.emptyList());
        File podcastByNumber = podcastFolderUtils.getPodcastByNumber(9);
        assertNull(podcastByNumber);
    }

    @Test(expected = NullPointerException.class)
    public void checkPodcastFolder_WithNullPath_ThrownException() {
        new PodcastFolderUtils(commandLine).checkPodcastFolder(null);
    }

    @Test(expected = RuntimeException.class)
    public void checkPodcastFolder_WithPodcastFolderIsNotExists_ThrownException() {
        PodcastFolderUtils podcastFolderUtils = new PodcastFolderUtils(commandLine);
        when(fileMock.exists()).thenReturn(false);
        podcastFolderUtils.checkPodcastFolder("test");
    }

    @Test(expected = RuntimeException.class)
    public void checkPodcastFolder_WithPodcastFileIsNotDirectory_ThrownException() {
        PodcastFolderUtils podcastFolderUtils = new PodcastFolderUtils(commandLine);
        when(fileMock.isDirectory()).thenReturn(false);
        podcastFolderUtils.checkPodcastFolder("test");
    }

    @Test
    public void checkPodcastPostsFolder_WithValidData() {
        new PodcastFolderUtils(commandLine);
    }

}
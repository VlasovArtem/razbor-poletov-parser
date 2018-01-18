package org.avlasov.razborpoletov.reader.parser.data;

import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.exception.PodcastLinkParseException;
import org.avlasov.razborpoletov.reader.matchers.PodcastLinkMatcher;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by artemvlasov on 04/07/2017.
 */
@PrepareForTest({Jsoup.class, PodcastFileUtils.class})
public class LinkParserTest extends PowerMockitoTestCase {

    @Mock
    private Connection connectionMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PodcastFolderUtils podcastFolderUtils;
    @InjectMocks
    private LinkParser linkParser;

    @Before
    public void setUp() throws Exception {
        Document document = Jsoup.parse(new File(this.getClass().getResource("razbor-poletov-archive.htm").getFile()), Charset.defaultCharset().name());
        mockStatic(Jsoup.class);
        when(Jsoup.connect(anyString())).thenReturn(connectionMock);
        when(connectionMock.get()).thenReturn(document);
        when(podcastFolderUtils.getAllPodcastFiles()).thenReturn(Collections.singletonList(new File("")));
        mockStatic(PodcastFileUtils.class);
    }

    @Test
    public void parsePodcastLink_WithValidPodcastNumber_ReturnOptionalWithData() {
        Optional<PodcastLink> podcastLink = linkParser.parsePodcastLink(122);
        assertTrue(podcastLink.isPresent());
        assertThat(podcastLink.get(), new PodcastLinkMatcher(new PodcastLink(122, "http://razbor-poletov.com/2016/12/episode-122.html")));
    }

    @Test
    public void parsePodcastLink_WithInvalidPodcastNumber_ReturnEmptyOptional() {
        Optional<PodcastLink> podcastLink = linkParser.parsePodcastLink(140);
        assertFalse(podcastLink.isPresent());
    }

    @Test
    public void parsePodcastLinks_WithValidArrayOfPodcastNumbers_ReturnCollection() {
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks(66, 130);
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(66, "http://razbor-poletov.com/2014/08/episode-66.html")));
    }

    @Test
    public void parsePodcastLinks_WithEmptyArrayOfPodcastNumbers_ReturnCollection() throws Exception {
        when(PodcastFileUtils.getPodcastsIdArray(anyListOf(File.class))).thenReturn(new int[]{30, 125});
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks((int[]) null);
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(30, "http://razbor-poletov.com/2012/12/episode-30.html")));
    }

    @Test
    public void parsePodcastLinks_WithArrayOfPodcastNumbersThatContainsNotExistingNumber_ReturnCollection() throws Exception {
        when(PodcastFileUtils.getPodcastsIdArray(anyListOf(File.class))).thenReturn(new int[]{30, 125, 140});
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks((int[]) null);
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(30, "http://razbor-poletov.com/2012/12/episode-30.html")));
    }

    @Test(expected = PodcastLinkParseException.class)
    public void parsePodcastLinks_WithMissingHtmlId_ReturnCollection() throws Exception {
        Document documentMock = mock(Document.class);
        when(connectionMock.get()).thenReturn(documentMock);
        when(documentMock.getElementById(anyString())).thenReturn(null);
        linkParser.parsePodcastLinks(33);
    }

    @Test
    public void parsePodcastLinks_WithJsoupExceptionThrown_ReturnEmptyCollection() throws Exception {
        Connection mock = mock(Connection.class);
        when(Jsoup.connect(anyString())).thenReturn(mock);
        when(mock.get()).thenThrow(new IOException());
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks((int[]) null);
        assertThat(podcastLinks, IsEmptyCollection.empty());
    }

    @Test
    public void parseAllPodcastLinks_WithValidData_ReturnCollection() throws Exception {
        when(PodcastFileUtils.getPodcastsIdArray(anyListOf(File.class))).thenReturn(new int[]{30, 125});
        List<PodcastLink> podcastLinks = linkParser.parseAllPodcastLinks();
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(30, "http://razbor-poletov.com/2012/12/episode-30.html")));
    }

    @Test
    public void parsePodcastLink_WithMultipleMatches_ReturnCollections() throws Exception {
        Document documentMock = PowerMockito.mock(Document.class, Answers.RETURNS_DEEP_STUBS.get());
        Document documentMock2 = PowerMockito.mock(Document.class, Answers.RETURNS_DEEP_STUBS.get());
        PowerMockito.when(documentMock.getElementById(anyString())).thenReturn(documentMock);
        PowerMockito.when(documentMock.getElementsByTag(eq("article"))).thenReturn(new Elements(documentMock, documentMock2));
        PowerMockito.when(documentMock.getElementsByTag(eq("a"))).thenReturn(new Elements(documentMock));
        PowerMockito.when(documentMock2.getElementsByTag(eq("a"))).thenReturn(new Elements(documentMock2));
        PowerMockito.when(documentMock.attr(anyString())).thenReturn("/2016/12/episode-10.html");
        PowerMockito.when(documentMock2.attr(anyString())).thenReturn("/2016/12/episode-10-test.html");
        PowerMockito.when(connectionMock.get()).thenReturn(documentMock);
        Optional<PodcastLink> podcastLink = linkParser.parsePodcastLink(10);
        assertTrue(podcastLink.isPresent());
    }

    @Test
    public void parse_WithFileCollection_ReturnPodcastLinkCollection() {
        when(PodcastFileUtils.getPodcastsIdArray(anyListOf(File.class))).thenReturn(new int[]{30, 125});
        List<PodcastLink> podcastLinks = linkParser.parse(Collections.singletonList(new File("test")));
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
    }

    @Test
    public void parse_WithNullPodcastArray_ReturnEmptyCollection() {
        when(PodcastFileUtils.getPodcastsIdArray(anyListOf(File.class))).thenReturn(null);
        List<PodcastLink> podcastLinks = linkParser.parse(Collections.singletonList(new File("test")));
        assertThat(podcastLinks, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithEmptyPodcastArray_ReturnEmptyCollection() {
        when(PodcastFileUtils.getPodcastsIdArray(anyListOf(File.class))).thenReturn(new int []{});
        List<PodcastLink> podcastLinks = linkParser.parse(Collections.singletonList(new File("test")));
        assertThat(podcastLinks, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithFile_ReturnPodcastLinkCollection() {
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(Optional.of(30));
        List<PodcastLink> podcastLinks = linkParser.parse(new File("test"));
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parse_WithEmptyPodcastNumber_ReturnEmptyCollection() {
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(Optional.empty());
        List<PodcastLink> podcastLinks = linkParser.parse(new File("test"));
        assertThat(podcastLinks, IsEmptyCollection.empty());
    }
}
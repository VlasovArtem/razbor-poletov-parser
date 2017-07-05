package org.avlasov.razborpoletov.reader.parser.data;

import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.exception.PodcastLinkParseException;
import org.avlasov.razborpoletov.reader.matchers.PodcastLinkMatcher;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by artemvlasov on 04/07/2017.
 */
@PrepareForTest({Jsoup.class, PodcastFileUtils.class})
public class LinkParserTest extends PowerMockitoTestCase {

    @Mock
    private Connection connectionMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FileParser fileParser;
    @InjectMocks
    private LinkParser linkParser;

    @Before
    public void setUp() throws Exception {
        Document document = Jsoup.parse(new File(this.getClass().getResource("razbor-poletov-archive.htm").getFile()), Charset.defaultCharset().name());
        mockStatic(Jsoup.class);
        when(Jsoup.connect(Mockito.anyString())).thenReturn(connectionMock);
        when(connectionMock.get()).thenReturn(document);
        when(fileParser.getPodcastsFiles()).thenReturn(Collections.singletonList(new File("")));
        mockStatic(PodcastFileUtils.class);
    }

    @Test
    public void parsePodcastLink_WithValidPodcastNumber_ReturnOptionalWithData() throws Exception {
        Optional<PodcastLink> podcastLink = linkParser.parsePodcastLink(122);
        assertTrue(podcastLink.isPresent());
        assertThat(podcastLink.get(), new PodcastLinkMatcher(new PodcastLink(122, "http://razbor-poletov.com/2016/12/episode-122.html")));
    }

    @Test
    public void parsePodcastLink_WithInvalidPodcastNumber_ReturnEmptyOptional() throws Exception {
        Optional<PodcastLink> podcastLink = linkParser.parsePodcastLink(140);
        assertFalse(podcastLink.isPresent());
    }

    @Test
    public void parsePodcastLinks_WithValidArrayOfPodcastNumbers_ReturnCollection() throws Exception {
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks(66, 130);
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(66, "http://razbor-poletov.com/2014/08/episode-66.html")));
    }

    @Test
    public void parsePodcastLinks_WithEmptyArrayOfPodcastNumbers_ReturnCollection() throws Exception {
        when(PodcastFileUtils.getPodcastsIdArray(Mockito.any(List.class))).thenReturn(new int[]{30, 125});
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks((int[]) null);
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(30, "http://razbor-poletov.com/2012/12/episode-30.html")));
    }

    @Test
    public void parsePodcastLinks_WithArrayOfPodcastNumbersThatContainsNotExistingNumber_ReturnCollection() throws Exception {
        when(PodcastFileUtils.getPodcastsIdArray(Mockito.any(List.class))).thenReturn(new int[]{30, 125, 140});
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks((int[]) null);
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(30, "http://razbor-poletov.com/2012/12/episode-30.html")));
    }

    @Test(expected = PodcastLinkParseException.class)
    public void parsePodcastLinks_WithMissingHtmlId_ReturnCollection() throws Exception {
        Document documentMock = mock(Document.class);
        when(connectionMock.get()).thenReturn(documentMock);
        when(documentMock.getElementById(Mockito.anyString())).thenReturn(null);
        linkParser.parsePodcastLinks(33);
    }

    @Test
    public void parsePodcastLinks_WithJsoupExceptionThrown_ReturnEmptyCollection() throws Exception {
        Connection mock = mock(Connection.class);
        when(Jsoup.connect(Mockito.anyString())).thenReturn(mock);
        when(mock.get()).thenThrow(new IOException());
        List<PodcastLink> podcastLinks = linkParser.parsePodcastLinks((int[]) null);
        assertThat(podcastLinks, IsEmptyCollection.empty());
    }

    @Test
    public void parseAllPodcastLinks_WithValidData_ReturnCollection() throws Exception {
        when(PodcastFileUtils.getPodcastsIdArray(Mockito.anyListOf(File.class))).thenReturn(new int[]{30, 125});
        List<PodcastLink> podcastLinks = linkParser.parseAllPodcastLinks();
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        assertThat(podcastLinks, IsCollectionContaining.hasItem(new PodcastLink(30, "http://razbor-poletov.com/2012/12/episode-30.html")));
    }

}
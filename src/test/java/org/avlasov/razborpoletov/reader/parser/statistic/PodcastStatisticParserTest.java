package org.avlasov.razborpoletov.reader.parser.statistic;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.StructuredDocument;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.entity.statistic.PodcastStatistic;
import org.avlasov.razborpoletov.reader.utils.AsciidocUtils;
import org.avlasov.razborpoletov.reader.utils.UrlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@PrepareForTest({PodcastStatisticParser.class, AsciidocUtils.class, Jsoup.class, UrlUtils.class})
public class PodcastStatisticParserTest extends PowerMockitoTestCase {

    @Mock
    private File file;
    @Mock
    private URL urlMock;
    @Mock
    private HttpURLConnection connection;
    @Mock
    private StructuredDocument structuredDocument;
    @Mock
    private ContentPart contentPart;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Document document;


    @Before
    public void setUp() throws Exception {
        mockStatic(AsciidocUtils.class, Jsoup.class);
        whenNew(URL.class).withAnyArguments().thenReturn(urlMock);
        PowerMockito.when(urlMock.openConnection()).thenReturn(connection);
        PowerMockito.when(connection.getContentLength()).thenReturn(10);
        PowerMockito.when(AsciidocUtils.parseDocument(any(File.class))).thenReturn(structuredDocument);
        PowerMockito.when(structuredDocument.getParts()).thenReturn(Collections.singletonList(contentPart));
        PowerMockito.when(contentPart.getContent()).thenReturn("libsyn href");
        PowerMockito.when(Jsoup.parse(anyString())).thenReturn(document);
        PowerMockito.when(document.getElementsByTag(anyString())).thenReturn(new Elements(document));
        PowerMockito.when(document.attributes().get(anyString())).thenReturn("libsyn");
        when(file.getAbsolutePath()).thenReturn("2012-01-18-episode-7.adoc");
    }

    @Test
    public void getMp3Filename_WithValidData_ReturnName() {
        String mp3Filename = new PodcastStatisticParser().getMp3Filename("test_name.mp3");
        assertEquals("test_name", mp3Filename);
    }

    @Test
    public void getMp3Filename_WithNotMatchingPattern_ReturnNull() {
        String mp3Filename = new PodcastStatisticParser().getMp3Filename("test_name");
        assertNull(mp3Filename);
    }

    @Test
    public void getMp3Filename_WithNullUrl_ReturnNull() {
        String mp3Filename = new PodcastStatisticParser().getMp3Filename(null);
        assertNull(mp3Filename);
    }

    @Test
    public void getMP3FileLength_WithValidData_ReturnInt() {
        assertEquals(10, new PodcastStatisticParser().getMP3FileLength("test"));
    }

    @Test
    public void getMP3FileLength_WithNullUrl_ReturnMinusOne() {
        assertEquals(-1, new PodcastStatisticParser().getMP3FileLength(null));
    }

    @Test
    public void getMP3FileLength_WithOpenConnectionThrowException_ReturnMinusOne() throws IOException {
        PowerMockito. when(urlMock.openConnection()).thenThrow(new IOException());
        assertEquals(-1, new PodcastStatisticParser().getMP3FileLength("test"));
    }

    @Test
    public void getUrl_WithValidData_ReturnUrl() {
        String url = new PodcastStatisticParser().getUrl(file);
        assertEquals("libsyn", url);
    }

    @Test
    public void getUrl_WithNotAsciidocFile_ReturnEmptyString() {
        when(file.getAbsolutePath()).thenReturn("2012-01-18-episode-7.html");
        String url = new PodcastStatisticParser().getUrl(file);
        assertEquals("", url);
    }

    @Test
    public void getUrl_WithMultipleElements_ReturnEmptyString() {
        PowerMockito.when(document.getElementsByTag(anyString())).thenReturn(new Elements(document, document));
        String url = new PodcastStatisticParser().getUrl(file);
        assertEquals("", url);
    }

    @Test
    public void getUrl_WithEmptyElements_ReturnEmptyString() {
        PowerMockito.when(document.getElementsByTag(anyString())).thenReturn(new Elements());
        String url = new PodcastStatisticParser().getUrl(file);
        assertEquals("", url);
    }

    @Before
    public void setUpForPodcastStatistic() throws Exception {
        mockStatic(UrlUtils.class);
        whenNew(File.class).withAnyArguments().thenReturn(file);
        InputStream inputStreamMock = mock(InputStream.class);
        FileOutputStream outputStreamMock = mock(FileOutputStream.class);
        Mp3File mp3FileMock = mock(Mp3File.class);
        when(inputStreamMock.read(any(byte[].class))).thenReturn(0);
        when(UrlUtils.getURL(anyString())).thenReturn(urlMock);
        when(file.getName()).thenReturn("test");
        when(connection.getInputStream()).thenReturn(inputStreamMock);
        whenNew(FileOutputStream.class).withAnyArguments().thenReturn(outputStreamMock);
        doNothing().when(outputStreamMock).write(any(byte[].class), anyInt(), anyInt());
        doNothing().when(outputStreamMock).close();
        whenNew(Mp3File.class).withParameterTypes(File.class).withArguments(file).thenReturn(mp3FileMock);
        when(mp3FileMock.getLengthInMilliseconds()).thenReturn(1000L);
        when(file.exists()).thenReturn(true);
    }

    @Test
    public void findPodcastData_WithValidData_ReturnPodcastStatistic() {
        PodcastStatistic podcastData = new PodcastStatisticParser().findPodcastData(Collections.singletonList(file));
        assertEquals(1000L, podcastData.getLongestPodcast());
        assertEquals(1000L, podcastData.getTotalPodcastsTime());
    }

    @Test
    public void findPodcastData_WithUrlConnectionThrownException_ReturnPodcastStatisticWithZeroData() throws Exception {
        PowerMockito.when(urlMock.openConnection()).thenThrow(new IOException());
        PodcastStatistic podcastData = new PodcastStatisticParser().findPodcastData(Collections.singletonList(file));
        assertEquals(0, podcastData.getLongestPodcast());
        assertEquals(0, podcastData.getTotalPodcastsTime());
    }

    @Test
    public void findPodcastData_WithMP3FileThrownException_ReturnPodcastStatisticWithZeroData() throws Exception {
        whenNew(Mp3File.class).withParameterTypes(File.class).withArguments(file).thenThrow(new InvalidDataException());
        PodcastStatistic podcastData = new PodcastStatisticParser().findPodcastData(Collections.singletonList(file));
        assertEquals(0, podcastData.getLongestPodcast());
        assertEquals(0, podcastData.getTotalPodcastsTime());
    }

    @Test
    public void findPodcastData_WithEmptyUrl_ReturnPodcastStatisticWithZeroData() {
        when(file.getAbsolutePath()).thenReturn("2012-01-18-episode-7.html");
        PodcastStatistic podcastData = new PodcastStatisticParser().findPodcastData(Collections.singletonList(file));
        assertEquals(0, podcastData.getLongestPodcast());
        assertEquals(0, podcastData.getTotalPodcastsTime());
    }

}
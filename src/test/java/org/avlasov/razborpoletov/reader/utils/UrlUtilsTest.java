package org.avlasov.razborpoletov.reader.utils;

import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@PrepareForTest({UrlUtils.class, Jsoup.class})
public class UrlUtilsTest extends PowerMockitoTestCase {

    @Mock
    private URI uri;
    @Mock
    private URL url;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpURLConnection urlConnection;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Document document;

    @Before
    public void setUp() throws Exception {
        whenNew(URI.class).withAnyArguments().thenReturn(uri);
        when(uri.toURL()).thenReturn(url);
        when(url.openConnection()).thenReturn(urlConnection);
        when(urlConnection.getResponseCode()).thenReturn(200);
    }

    @Before
    public void setUpFindGithubLinks() throws Exception {
        mockStatic(Jsoup.class);
        when(Jsoup.parse(any(InputStream.class), anyString(), anyString())).thenReturn(document);
        when(document.getElementsByTag(anyString())).thenReturn(new Elements(document));
        when(document.attr(anyString())).thenReturn("https://github.com/test/test");
    }

    @Test
    public void findGithubLink_WithValidData_ReturnLink() throws Exception {
        assertNotNull(UrlUtils.findGithubLink("test"));
    }

    @Test
    public void findGithubLink_WithWithHttpGithubLink_ReturnLink() throws Exception {
        when(document.attr(anyString())).thenReturn("http://github.com/test/test");
        assertNotNull(UrlUtils.findGithubLink("test"));
    }

    @Test
    public void findGithubLink_WithNotMatchingUrl_ReturnNull() throws Exception {
        when(document.attr(anyString())).thenReturn("https://test.com");
        assertNull(UrlUtils.findGithubLink("test"));
    }

    @Test
    public void findGithubLink_WithInvalidStatus_ReturnNull() throws Exception {
        when(urlConnection.getResponseCode()).thenReturn(400);
        assertNull(UrlUtils.findGithubLink("test"));
    }

    @Test
    public void findGithubLink_WithMultipleGithubLinks_ReturnNull() throws Exception {
        Document mock = mock(Document.class);
        when(mock.attr(anyString())).thenReturn("https://github.com/test/test2");
        when(document.getElementsByTag(anyString())).thenReturn(new Elements(document, mock));
        assertNull(UrlUtils.findGithubLink("test"));
    }

    @Test
    public void checkUrlStatus_WithValidURL_ReturnValidStatus() {
        assertEquals(200, UrlUtils.checkUrlStatus("http://test.com"));
    }

    @Test
    public void checkUrlStatus_WithURIThornException_ReturnZero() throws Exception {
        when(uri.toURL()).thenThrow(new MalformedURLException("Test"));
        assertEquals(0, UrlUtils.checkUrlStatus("http://test.com"));
    }

    @Test
    public void checkUrlStatus_WithIgnoredURL_ReturnZero() {
        assertEquals(0, UrlUtils.checkUrlStatus("http://www.latencytop.org/"));
    }

    @Test
    public void checkUrlStatus_WithHttpURLConnectionThrowException_ReturnZero() throws Exception {
        when(urlConnection.getResponseCode()).thenThrow(new IOException());
        assertEquals(0, UrlUtils.checkUrlStatus("http://test.com"));
    }

    @Test
    public void getURL_WithValidData_ReturnUrl() throws Exception {
        URL test = UrlUtils.getURL("test");
        assertNotNull(test);
    }

    @Test(expected = MalformedURLException.class)
    public void getURL_WithURIThrowException_ThrownException() throws Exception {
        when(uri.toURL()).thenThrow(new MalformedURLException("Test"));
        UrlUtils.getURL("http://test");
    }

}
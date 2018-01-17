package org.avlasov.razborpoletov.reader.utils;

import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;
import java.net.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@PrepareForTest({UrlUtils.class})
public class UrlUtilsTest extends PowerMockitoTestCase {

    @Mock
    private URI uri;
    @Mock
    private URL url;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpURLConnection urlConnection;

    @Before
    public void setUp() throws Exception {
        whenNew(URI.class).withAnyArguments().thenReturn(uri);
        when(uri.toURL()).thenReturn(url);
        when(url.openConnection()).thenReturn(urlConnection);
        when(urlConnection.getResponseCode()).thenReturn(200);
    }

    @Test
    public void findGithubLink() {
    }

    @Test
    public void getGithubDescription() {
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
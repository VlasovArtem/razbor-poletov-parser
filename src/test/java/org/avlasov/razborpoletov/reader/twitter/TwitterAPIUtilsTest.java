package org.avlasov.razborpoletov.reader.twitter;

import org.avlasov.razborpoletov.reader.exception.TwitterAPIInvalidCredentialsException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created By artemvlasov on 04/01/2018
 **/
@PowerMockIgnore({"javax.management.*"})
@RunWith(PowerMockRunner.class)
@TestPropertySource(properties = {
        "ConsumerKey=test",
        "ConsumerSecret=test2"
})
@PowerMockRunnerDelegate(SpringRunner.class)
@ContextConfiguration(classes = {TwitterAPIUtils.class})
@PrepareForTest(value = {TwitterAPIUtils.class})
public class TwitterAPIUtilsTest {

    @Autowired
    private TwitterAPIUtils twitterAPIUtils;
    @Mock
    private RestTemplate restTemplate;

    @Test
    public void getAccessToken_WithValidResponse_ReturnTwitterAccessToken() throws Exception {
        whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
        when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(TwitterAccessToken.class))).thenReturn(new ResponseEntity<>(new TwitterAccessToken("test", "test"), HttpStatus.FOUND));
        TwitterAccessToken accessToken = twitterAPIUtils.getAccessToken();
        assertNotNull(accessToken);
        assertEquals("test", accessToken.getAccessToken());
    }

    @Test(expected = TwitterAPIInvalidCredentialsException.class)
    public void getAccessToken_WithEmptyCustomerKey_ThrowException() throws NoSuchFieldException, IllegalAccessException {
        TwitterAPIUtils twitterAPIUtils = new TwitterAPIUtils();
        Field consumerSecret = twitterAPIUtils.getClass().getDeclaredField("consumerSecret");
        consumerSecret.setAccessible(true);
        consumerSecret.set(twitterAPIUtils, "test");
        twitterAPIUtils.getAccessToken();
    }

    @Test(expected = TwitterAPIInvalidCredentialsException.class)
    public void getAccessToken_WithEmptyCustomerSecret_ThrowException() throws NoSuchFieldException, IllegalAccessException {
        TwitterAPIUtils twitterAPIUtils = new TwitterAPIUtils();
        Field consumerKey = twitterAPIUtils.getClass().getDeclaredField("consumerKey");
        consumerKey.setAccessible(true);
        consumerKey.set(twitterAPIUtils, "test");
        twitterAPIUtils.getAccessToken();
    }

}
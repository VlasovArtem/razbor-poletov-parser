package org.avlasov.razborpoletov.reader.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.avlasov.razborpoletov.reader.twitter.entity.TwitterUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created By artemvlasov on 03/01/2018
 **/
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {TwitterAPI.class, TwitterAccessToken.class, TwitterAPIUtils.class})
@PowerMockIgnore({"javax.management.*"})
public class TwitterAPITest {

    @Mock
    private TwitterAPIUtils twitterAPIUtils;
    @Mock
    private RestTemplate restTemplate;
    private TwitterAPI twitterAPI;

    @Before
    public void setUp() throws Exception {
        whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
        when(twitterAPIUtils.getAccessToken()).thenReturn(new TwitterAccessToken("test", "test"));
        twitterAPI = new TwitterAPI(twitterAPIUtils);
    }

    @Test
    public void getTwitterUser_WithValidScreenName_ReturnTwitterUserOptional() throws IOException {
        ResponseEntity<TwitterUser> tResponseEntity = new ResponseEntity<>(getTwitterUser(), HttpStatus.FOUND);
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(TwitterUser.class)))
                .thenReturn(tResponseEntity);
        Optional<TwitterUser> user = twitterAPI.getTwitterUser("test");
        assertTrue(user.isPresent());
        assertEquals("gAmUssA", user.get().getScreenName());
    }

    @Test
    public void getTwitterUser_WithScreenNameClientError_ReturnEmptyOptional() throws IOException {
        ResponseEntity<TwitterUser> tResponseEntity = new ResponseEntity<>(new TwitterUser(), HttpStatus.NOT_FOUND);
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(TwitterUser.class)))
                .thenReturn(tResponseEntity);
        Optional<TwitterUser> user = twitterAPI.getTwitterUser("test");
        assertFalse(user.isPresent());
    }

    private TwitterUser getTwitterUser() throws IOException {
        String file = TwitterAPITest.class.getResource("./twitter_user_example.json").getFile();
        File userJson = new File(file);
        ObjectMapper build = Jackson2ObjectMapperBuilder.json()
                .failOnUnknownProperties(false)
                .build();
        return build.readValue(userJson, TwitterUser.class);
    }

}
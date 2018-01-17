package org.avlasov.razborpoletov.reader.twitter;

import org.avlasov.razborpoletov.reader.exception.TwitterAPIInvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * Created By artemvlasov on 03/01/2018
 **/
@Component
@PropertySource("classpath:twitter.properties")
public class TwitterAPIUtils {

    @Value("${ConsumerKey}")
    private String consumerKey;
    @Value("${ConsumerSecret}")
    private String consumerSecret;

    public TwitterAccessToken getAccessToken() {
        validateTwitterAPICredentials();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors()
                .add(new BasicAuthorizationInterceptor(consumerKey, consumerSecret));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> objectHttpEntity = new HttpEntity<>(map, headers);
        ResponseEntity<TwitterAccessToken> accessToken = restTemplate.postForEntity("https://api.twitter.com/oauth2/token", objectHttpEntity, TwitterAccessToken.class);
        if (accessToken.getStatusCode().is5xxServerError() || accessToken.getStatusCode().is4xxClientError()) {
            throw new RuntimeException("URL to retrieve access token send error. " + accessToken.getStatusCode().toString() + " " + accessToken.toString());
        }
        return accessToken.getBody();
    }

    private void validateTwitterAPICredentials() {
        if (Objects.toString(consumerKey, "").isEmpty() || Objects.toString(consumerSecret, "").isEmpty()) {
            throw new TwitterAPIInvalidCredentialsException("ConsumerKey or ConsumerSecret is empty. Please add this properties to the file twitter.properties.");
        }
    }

}

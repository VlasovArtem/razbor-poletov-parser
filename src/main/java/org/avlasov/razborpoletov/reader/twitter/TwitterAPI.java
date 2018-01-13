package org.avlasov.razborpoletov.reader.twitter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.twitter.entity.TwitterUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Created By artemvlasov on 03/01/2018
 **/
@Component
public class TwitterAPI {

    private final static Logger LOGGER = LogManager.getLogger(TwitterAPI.class);
    private RestTemplate restTemplate;
    private HttpHeaders tokenHeaders;

    @Autowired
    public TwitterAPI(TwitterAPIUtils utils) {
        this.restTemplate = new RestTemplate();
        TwitterAccessToken accessToken = utils.getAccessToken();
        tokenHeaders = new HttpHeaders();
        tokenHeaders.add("Authorization", String.format("%s %s", StringUtils.capitalize(accessToken.getTokenType()), accessToken.getAccessToken()));
    }

    public Optional<TwitterUser> getTwitterUser(String screenName) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://api.twitter.com/1.1/users/show.json")
                .queryParam("screen_name", screenName);
        return getEntity(builder, TwitterUser.class);
    }

    public Optional<TwitterUser> getTwitterUser(long userId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://api.twitter.com/1.1/users/show.json")
                .queryParam("user_id", userId);
        return getEntity(builder, TwitterUser.class);
    }

    private <T> Optional<T> getEntity(UriComponentsBuilder builder, Class<T> clazz) {
        try {
            LOGGER.debug("Twitter API uri {}", builder.toUriString());
            HttpEntity<?> entity = new HttpEntity<>(tokenHeaders);
            ResponseEntity<T> exchange = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, entity, clazz);
            if (exchange.getStatusCode().is4xxClientError() || exchange.getStatusCode().is5xxServerError()) {
                LOGGER.info("Twitter api return error status code. Status code - {}, Message - {}", exchange.getStatusCode().toString(), exchange.toString());
                return Optional.empty();
            }
            return Optional.of(exchange.getBody());
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                LOGGER.error("Link {} throw exception with message {}.", builder.toUriString(), e.getMessage());
            }
            LOGGER.error(e);
            return Optional.empty();
        }
    }

}

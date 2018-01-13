package org.avlasov.razborpoletov.reader.twitter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created By artemvlasov on 03/01/2018
 **/
public class TwitterAccessToken {

    @JsonProperty(value = "token_type")
    private String tokenType;
    @JsonProperty(value = "access_token")
    private String accessToken;

    public TwitterAccessToken() {
    }

    public TwitterAccessToken(String tokenType, String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }
}

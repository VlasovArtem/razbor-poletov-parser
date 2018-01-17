package org.avlasov.razborpoletov.reader.twitter;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class TwitterAccessTokenTest {

    @Test
    public void defaultConstructor() {
        new TwitterAccessToken();
    }

    @Test
    public void constructorWithArguments() {
        new TwitterAccessToken("test", "test");
    }

    @Test
    public void getTokenType() {
        assertNotNull(new TwitterAccessToken("test", "test").getTokenType());
    }

    @Test
    public void getAccessToken() {
        assertNotNull(new TwitterAccessToken("test", "test").getAccessToken());
    }
}
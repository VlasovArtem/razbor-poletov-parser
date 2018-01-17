package org.avlasov.razborpoletov.reader.twitter.entity;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class TwitterUserTest {

    @Test
    public void defaultConstructor() {
        new TwitterUser();
    }

    @Test
    public void getId() {
        assertNotNull(getBuilder().build().getId());
    }

    @Test
    public void getName() {
        assertNotNull(getBuilder().build().getName());
    }

    @Test
    public void getScreenName() {
        assertNotNull(getBuilder().build().getScreenName());
    }

    @Test
    public void getLocation() {
        assertNotNull(getBuilder().build().getLocation());
    }

    @Test
    public void getDescription() {
        assertNotNull(getBuilder().build().getDescription());
    }

    @Test
    public void getProfileImageUrl() {
        assertNotNull(getBuilder().build().getProfileImageUrl());
    }

    @Test
    public void equals() {
        assertTrue(getBuilder().build().equals(getBuilder().build()));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(getBuilder().build().hashCode(), getBuilder().build().hashCode());
    }

    @Test
    public void compareTo() {
        assertEquals(0, getBuilder().build().compareTo(getBuilder().build()));
    }

    @Test
    public void builder() {
        assertNotNull(getBuilder());
    }

    private TwitterUser.TwitterUserBuilder getBuilder() {
        return TwitterUser.builder()
                .screenName("Test")
                .profileImageUrl("test")
                .name("test")
                .location("test")
                .id(10)
                .description("test");
    }

}
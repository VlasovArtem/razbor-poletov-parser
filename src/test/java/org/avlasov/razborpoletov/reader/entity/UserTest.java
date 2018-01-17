package org.avlasov.razborpoletov.reader.entity;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class UserTest {

    @Test
    public void builder() {
        assertNotNull(getBuilder());
    }

    @Test
    public void builder_WithUserSource() {
        assertNotNull(User.builder(getBuilder().build()));
    }

    @Test
    public void getAppearanceEpisodeNumbers() {
        assertThat(getBuilder().build().getAppearanceEpisodeNumbers(), IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void getTwitterId() {
        assertEquals(10, getBuilder().build().getTwitterId());
    }

    @Test
    public void getTwitterAccount() {
        assertNotNull(getBuilder().build().getTwitterAccount());
    }

    @Test
    public void getTwitterAccountUrl() {
        assertNotNull(getBuilder().build().getTwitterAccountUrl());
    }

    @Test
    public void getName() {
        assertNotNull(getBuilder().build().getName());
    }

    @Test
    public void getTwitterImgUrl() {
        assertNotNull(getBuilder().build().getTwitterImgUrl());
    }

    @Test
    public void getEpisodeNumberOfTheFirstAppearance() {
        assertEquals(10, getBuilder().build().getEpisodeNumberOfTheFirstAppearance());
    }

    @Test
    public void getLocation() {
        assertNotNull(getBuilder().build().getLocation());
    }

    @Test
    public void getBio() {
        assertNotNull(getBuilder().build().getBio());
    }

    @Test
    public void getTotalAppearance() {
        assertEquals(1, getBuilder().build().getTotalAppearance());
    }

    @Test
    public void equals() {
        assertTrue(getBuilder().build().equals(User.builder().twitterId(10).build()));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(getBuilder().build().hashCode(), User.builder().twitterId(10).build().hashCode());
    }

    @Test
    public void toStringTest() {
        assertNotNull(getBuilder().build().toString());
    }

    @Test
    public void compareTo() {
        assertEquals(0, getBuilder().build().compareTo(User.builder().twitterId(10).addEpisode(11).build()));
    }

    private User.UserBuilder getBuilder() {
        return User.builder()
                .addEpisode(10)
                .twitterImgUrl("test")
                .name("test")
                .twitterId(10)
                .twitterAccountUrl("test")
                .twitterAccount("test")
                .location("test")
                .bio("test");
    }

}
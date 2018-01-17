package org.avlasov.razborpoletov.reader.entity.info;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class UsefulThingTest {

    @Test
    public void defaultConstructor() {
        new UsefulThing();
    }

    @Test
    public void getLink() {
        assertNotNull(getBuilder().build().getLink());
    }

    @Test
    public void getTags() {
        assertThat(getBuilder().build().getTags(), IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void getName() {
        assertNotNull(getBuilder().build().getName());
    }

    @Test
    public void isChecked() {
        assertTrue(getBuilder().build().isChecked());
    }

    @Test
    public void getPodcastId() {
        assertEquals(10, getBuilder().build().getPodcastId());
    }

    @Test
    public void getDescription() {
        assertNotNull(getBuilder().build().getDescription());
    }

    @Test
    public void builder() {
        assertNotNull(getBuilder());
    }

    private UsefulThing.UsefulThingBuilder getBuilder() {
        return UsefulThing.builder()
                .description("Test")
                .podcastId(10)
                .checked(true)
                .tags(Collections.singletonList("test"))
                .name("test")
                .link("test link");
    }
}
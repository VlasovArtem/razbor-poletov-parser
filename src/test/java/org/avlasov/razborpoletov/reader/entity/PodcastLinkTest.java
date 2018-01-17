package org.avlasov.razborpoletov.reader.entity;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class PodcastLinkTest {

    @Test
    public void getPodcastNumber() {
        assertEquals(7, getPodcastLink().getPodcastNumber());
    }

    @Test
    public void getLink() {
        assertNotNull(getPodcastLink().getLink());
    }

    @Test
    public void equals() {
        assertTrue(getPodcastLink().equals(getPodcastLink()));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(getPodcastLink().hashCode(), getPodcastLink().hashCode());
    }

    @Test
    public void compareTo() {
        assertEquals(0, getPodcastLink().compareTo(getPodcastLink()));
    }

    private PodcastLink getPodcastLink() {
        return new PodcastLink(7, "test");
    }

}
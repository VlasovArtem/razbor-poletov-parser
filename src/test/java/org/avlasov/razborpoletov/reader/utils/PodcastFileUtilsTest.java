package org.avlasov.razborpoletov.reader.utils;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created By artemvlasov on 16/01/2018
 **/
public class PodcastFileUtilsTest {

    @Test
    public void getPodcastDate_WithValidFile_ReturnLocaleDate() {
        LocalDate podcastDate = PodcastFileUtils.getPodcastDate(new File("2012-01-18-episode-7.adoc"));
        assertNotNull(podcastDate);
        assertEquals(2012, podcastDate.getYear());
    }

    @Test
    public void getPodcastDate_WithInvalidFile_ReturnNull() {
        LocalDate podcastDate = PodcastFileUtils.getPodcastDate(new File(""));
        assertNull(podcastDate);
    }

    @Test
    public void getPodcastNumber_WithValidFile_ReturnNumber() {
        Optional<Integer> podcastNumber = PodcastFileUtils.getPodcastNumber(new File("2012-01-18-episode-7.adoc"));
        assertTrue(podcastNumber.isPresent());
        assertEquals(7, (long) podcastNumber.get());
    }

    @Test
    public void getPodcastNumber_WithNullFile_ReturnEmptyOptional() {
        Optional<Integer> podcastNumber = PodcastFileUtils.getPodcastNumber(null);
        assertFalse(podcastNumber.isPresent());
    }

    @Test
    public void getPodcastNumber_WithInvalidFile_ReturnEmptyOptional() {
        Optional<Integer> podcastNumber = PodcastFileUtils.getPodcastNumber(new File(""));
        assertFalse(podcastNumber.isPresent());
    }

    @Test
    public void getPodcastsId_WithValidFiles_ReturnNumberCollection() {
        List<Integer> podcastsId = PodcastFileUtils.getPodcastsId(Collections.singletonList(new File("2012-01-18-episode-7.adoc")));
        assertThat(podcastsId, IsCollectionWithSize.hasSize(1));
        assertThat(podcastsId, IsCollectionContaining.hasItem(7));
    }

    @Test
    public void getPodcastsId_WithNullFiles_ReturnEmptyCollection() {
        List<Integer> podcastsId = PodcastFileUtils.getPodcastsId(null);
        assertThat(podcastsId, IsEmptyCollection.empty());
    }

    @Test
    public void getPodcastsIdArray_WithValidFiles_ReturnArray() {
        int[] podcastsIdArray = PodcastFileUtils.getPodcastsIdArray(Collections.singletonList(new File("2012-01-18-episode-7.adoc")));
        assertArrayEquals(new int[]{7}, podcastsIdArray);
    }
}
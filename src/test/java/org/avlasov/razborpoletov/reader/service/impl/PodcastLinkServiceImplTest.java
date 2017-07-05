package org.avlasov.razborpoletov.reader.service.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.exception.PodcastLinksServiceException;
import org.avlasov.razborpoletov.reader.matchers.PodcastLinkMatcher;
import org.avlasov.razborpoletov.reader.parser.data.LinkParser;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by artemvlasov on 05/07/2017.
 */
@PrepareForTest({PodcastFileUtils.class, ObjectMapper.class})
public class PodcastLinkServiceImplTest extends PowerMockitoTestCase {

    @Mock
    private LinkParser linkParser;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private PodcastLinkServiceImpl podcastLinkService;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void parseAllPodcastLinks_WithValidData_ReturnCollection() throws Exception {
        when(linkParser.parseAllPodcastLinks()).thenReturn(Collections.singletonList(new PodcastLink(33, "test")));
        assertThat(podcastLinkService.parseAllPodcastLinks(), IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parsePodcastLinks_WithValidArray_ReturnCollection() throws Exception {
        when(linkParser.parsePodcastLinks(Mockito.anyVararg())).thenReturn(Collections.singletonList(new PodcastLink(33, "test")));
        assertThat(podcastLinkService.parsePodcastLinks(33), IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parsePodcastLinks_WithValidList_ReturnCollection() throws Exception {
        mockStatic(PodcastFileUtils.class);
        when(PodcastFileUtils.getPodcastsIdArray(Mockito.anyListOf(File.class))).thenReturn(new int[0]);
        when(linkParser.parsePodcastLinks(Mockito.anyVararg())).thenReturn(Collections.singletonList(new PodcastLink(33, "test")));
        assertThat(podcastLinkService.parsePodcastLinks(Collections.singletonList(new File(""))), IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parsePodcastLinks_WithNullList_ReturnEmptyCollection() throws Exception {
        assertThat(podcastLinkService.parsePodcastLinks((List<File>) null), IsEmptyCollection.empty());
    }

    @Test
    public void parsePodcastLinks_WithEmptyList_ReturnEmptyCollection() throws Exception {
        assertThat(podcastLinkService.parsePodcastLinks(Collections.emptyList()), IsEmptyCollection.empty());
    }

    @Test
    public void parsePodcastLink_WithValidFile_ReturnOptional() throws Exception {
        mockStatic(PodcastFileUtils.class);
        when(PodcastFileUtils.getPodcastId(Mockito.any(File.class))).thenReturn(Optional.of(40));
        when(linkParser.parsePodcastLink(Mockito.anyInt())).thenReturn(Optional.of(new PodcastLink(40, "test")));
        Optional<PodcastLink> podcastLink = podcastLinkService.parsePodcastLink(new File(""));
        assertTrue(podcastLink.isPresent());
    }

    @Test
    public void parsePodcastLink_WithNullFile_ReturnEmptyOptional() throws Exception {
        Optional<PodcastLink> podcastLink = podcastLinkService.parsePodcastLink(null);
        assertFalse(podcastLink.isPresent());
    }

    @Test
    public void parsePodcastLink_WithEmptyPodcastNumber_ReturnEmptyOptional() throws Exception {
        mockStatic(PodcastFileUtils.class);
        when(PodcastFileUtils.getPodcastId(Mockito.any(File.class))).thenReturn(Optional.empty());
        Optional<PodcastLink> podcastLink = podcastLinkService.parsePodcastLink(new File(""));
        assertFalse(podcastLink.isPresent());
    }

    @Test
    public void parsePodcastLink_WithValidaPodcastNumber_ReturnOptional() throws Exception {
        when(linkParser.parsePodcastLink(33)).thenReturn(Optional.of(new PodcastLink(33, "test")));
        assertTrue(podcastLinkService.parsePodcastLink(33).isPresent());
    }

    @Test
    public void savePodcastLinksToJson_WithValidDataAppendTrue_ReturnOptional() throws Exception {
        File fileMock = mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(fileMock);
        PodcastLinkServiceImpl spy = spy(podcastLinkService);
        PowerMockito.when(spy.prepareLinksForSave(Mockito.anyListOf(PodcastLink.class), Mockito.anyBoolean())).thenReturn(Collections.singletonList(new PodcastLink(33, "test")));
        PowerMockito.doNothing().when(objectMapper).writeValue(Mockito.any(File.class), Mockito.anyListOf(PodcastLink.class));
        Optional<File> test = spy.savePodcastLinksToJson(Collections.singletonList(new PodcastLink(33, "test")), true);
        assertTrue(test.isPresent());
    }

    @Test
    public void savePodcastLinksToJson_WithValidDataAppendFalse_ReturnOptional() throws Exception {
        File fileMock = mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(fileMock);
        PodcastLinkServiceImpl spy = spy(podcastLinkService);
        PowerMockito.when(spy.prepareLinksForSave(Mockito.anyListOf(PodcastLink.class), Mockito.anyBoolean())).thenReturn(Collections.singletonList(new PodcastLink(33, "test")));
        PowerMockito.doNothing().when(objectMapper).writeValue(Mockito.any(File.class), Mockito.anyListOf(PodcastLink.class));
        Optional<File> test = spy.savePodcastLinksToJson(Collections.singletonList(new PodcastLink(33, "test")), false);
        assertTrue(test.isPresent());
    }

    @Test
    public void savePodcastLinksToJson_WithNullPodcastLinks_ReturnEmptyOptional() throws Exception {
        assertFalse(podcastLinkService.savePodcastLinksToJson((List<PodcastLink>) null, true).isPresent());
    }

    @Test
    public void savePodcastLinksToJson_WithEmptyPodcastLinks_ReturnEmptyOptional() throws Exception {
        assertFalse(podcastLinkService.savePodcastLinksToJson(Collections.emptyList(), true).isPresent());
    }

    @Test(expected = PodcastLinksServiceException.class)
    public void savePodcastLinksToJson_WithObjectMapperThrowException_ExceptionThrown() throws Exception {
        PodcastLinkServiceImpl spy = spy(podcastLinkService);
        PowerMockito.when(spy.prepareLinksForSave(Mockito.anyListOf(PodcastLink.class), Mockito.anyBoolean())).thenReturn(Collections.singletonList(new PodcastLink(33, "test")));
        PowerMockito.doThrow(new IOException()).when(objectMapper).writeValue(Mockito.any(File.class), Mockito.anyListOf(PodcastLink.class));
        assertTrue(spy.savePodcastLinksToJson(Collections.singletonList(new PodcastLink(33, "test")), true).isPresent());
    }

    @Test
    public void savePodcastLinksToJson_WithValidPodcastLink_ReturnOptional() throws Exception {
        PodcastLinkServiceImpl spy = spy(podcastLinkService);
        PowerMockito.when(spy.savePodcastLinksToJson(Mockito.anyListOf(PodcastLink.class), Mockito.anyBoolean())).thenReturn(Optional.of(new File("")));
        assertTrue(spy.savePodcastLinksToJson(new PodcastLink(33, "test"), true).isPresent());
    }

    @Test
    public void prepareLinksForSave_WithAppendAndMatchingPodcastLinks_ReturnCollection() throws Exception {
        when(objectMapper.getTypeFactory()).thenReturn(new ObjectMapper().getTypeFactory());
        when(objectMapper.readValue(Mockito.any(File.class), Mockito.any(JavaType.class))).thenReturn(Collections.singletonList(new PodcastLink(33, "old")));
        List<PodcastLink> podcastLinks = Arrays.asList(new PodcastLink(33, "new"), new PodcastLink(40, "test"));
        List<PodcastLink> updatedLinks = podcastLinkService.prepareLinksForSave(podcastLinks, true);
        assertThat(updatedLinks, IsCollectionWithSize.hasSize(2));
        assertThat(updatedLinks, IsCollectionContaining.hasItem(new PodcastLinkMatcher(new PodcastLink(33, "new"))));
    }

    @Test
    public void prepareLinksForSave_WithAppendAndNonMatchingPodcastLinks_ReturnCollection() throws Exception {
        when(objectMapper.getTypeFactory()).thenReturn(new ObjectMapper().getTypeFactory());
        when(objectMapper.readValue(Mockito.any(File.class), Mockito.any(JavaType.class))).thenReturn(Collections.singletonList(new PodcastLink(33, "test")));
        List<PodcastLink> podcastLinks = Arrays.asList(new PodcastLink(34, "new"), new PodcastLink(40, "test"));
        List<PodcastLink> updatedLinks = podcastLinkService.prepareLinksForSave(podcastLinks, true);
        assertThat(updatedLinks, IsCollectionWithSize.hasSize(3));
        assertThat(updatedLinks, IsCollectionContaining.hasItem(new PodcastLinkMatcher(new PodcastLink(34, "new"))));
    }

    @Test
    public void prepareLinksForSave_WithAppendFalse_ReturnCollection() throws Exception {
        List<PodcastLink> podcastLinks = Arrays.asList(new PodcastLink(34, "new"), new PodcastLink(40, "test"));
        List<PodcastLink> updatedLinks = podcastLinkService.prepareLinksForSave(podcastLinks, false);
        assertThat(updatedLinks, IsCollectionWithSize.hasSize(2));
        assertThat(updatedLinks, IsCollectionContaining.hasItem(new PodcastLinkMatcher(new PodcastLink(34, "new"))));
    }

    @Test(expected = PodcastLinksServiceException.class)
    public void prepareLinksForSave_WithObjectMapperThrowException_ExceptionThrown() throws Exception {
        when(objectMapper.getTypeFactory()).thenReturn(new ObjectMapper().getTypeFactory());
        doThrow(new IOException()).when(objectMapper).readValue(Mockito.any(File.class), Mockito.any(CollectionLikeType.class));
        podcastLinkService.prepareLinksForSave(Collections.singletonList(new PodcastLink(34, "new")), true);
    }
}
package org.avlasov.razborpoletov.reader.service.data.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.cli.ParserCommandLine;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.parser.data.LinkParser;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 08/01/2018
 **/
@PrepareForTest(value = {LinkServiceDataImpl.class, PodcastFileUtils.class, PodcastFolderUtils.class})
public class LinkServiceDataImplTest extends PowerMockitoTestCase {

    @Mock
    private PodcastFolderUtils folderUtils;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private File mockFile;
    @Mock
    private ParserCommandLine parserCommandLine;
    @Mock
    private LinkParser linkParser;
    @Mock
    private LinkServiceDataImpl linkServiceData;

    @Before
    public void setUp() throws Exception {
        List<File> files = Collections.singletonList(mockFile);
        whenNew(File.class).withAnyArguments().thenReturn(mockFile);
        when(objectMapper.readValue(Mockito.any(File.class), Mockito.any(JavaType.class)))
                .thenReturn(getJsonData());
        mockStatic(PodcastFileUtils.class);
        when(PodcastFileUtils.getPodcastNumber(Mockito.any(File.class))).thenReturn(Optional.of(160));
        when(folderUtils.getLastPodcastFile()).thenReturn(mockFile);
        when(folderUtils.getPodcastsFiles(Mockito.anyInt(), Mockito.anyInt())).thenReturn(files);
        when(linkParser.parse(Mockito.anyListOf(File.class))).thenReturn(getNewData());
        when(mockFile.exists()).thenReturn(true);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        doNothing().when(objectMapper).writeValue(any(File.class), any());
        linkServiceData = new LinkServiceDataImpl(linkParser, objectMapper, parserCommandLine, folderUtils);
    }

    @Test
    public void getDataClass() {
        assertTrue(PodcastLink.class.equals(linkServiceData.getDataClass()));
    }

    @Test
    public void mergeUsersData_WithExistingData_ReturnMergedCollection() {
        List<PodcastLink> podcastLinks = linkServiceData.mergeUsersData(getJsonData(), getNewData());
        assertThat(podcastLinks, IsCollectionWithSize.hasSize(2));
        Optional<PodcastLink> first = podcastLinks.stream()
                .filter(podcastLink -> podcastLink.getPodcastNumber() == 130)
                .findFirst();
        assertTrue(first.isPresent());
        assertEquals(first.get().getLink(), "/2013/11/new-episode-130.html");
    }

    @Test
    public void getLastParsedPodcastNumber_WithExistingJsonData_ReturnNumber() {
        assertEquals(130, linkServiceData.getLastParsedPodcastNumber());
    }

    @Test
    public void getLastParsedPodcastNumber_WithNotJsonData_ReturnStubNumber() throws IOException {
        when(objectMapper.readValue(Mockito.any(File.class), Mockito.any(JavaType.class))).thenReturn(Collections.emptyList());
        linkServiceData = new LinkServiceDataImpl(linkParser, objectMapper, parserCommandLine, folderUtils);
        assertEquals(-999, linkServiceData.getLastParsedPodcastNumber());
    }

    @Test
    public void getFileName() {
        assertEquals("links", linkServiceData.getFileName());
    }

    private List<PodcastLink> getJsonData() {
        return getList(new PodcastLink(130, "/2013/11/episode-130.html"));
    }

    private List<PodcastLink> getNewData() {
        return getList(new PodcastLink(130, "/2013/11/new-episode-130.html"),
                new PodcastLink(131, "/2013/11/new-episode-131.html"));
    }

    private List<PodcastLink> getList(PodcastLink... links) {
        return new ArrayList<>(Arrays.asList(links));
    }

}
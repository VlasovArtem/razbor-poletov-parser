package org.avlasov.razborpoletov.reader.parser.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.io.FilenameUtils;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.entity.info.UsefulThing;
import org.avlasov.razborpoletov.reader.utils.*;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created By artemvlasov on 14/01/2018
 **/
@PrepareForTest({UsefulThingParser.class, PodcastFileUtils.class, FilenameUtils.class, AsciidocUtils.class, UrlUtils.class, Jsoup.class, MarkdownUtils.class})
public class UsefulThingParserTest extends PowerMockitoTestCase {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Element element;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private File file;
    @Mock
    private Document documentMock;
    private UsefulThingParser usefulThingParser;

    @Before
    public void jacksonConfiguration() throws Exception {
        whenNew(ObjectMapper.class).withAnyArguments().thenReturn(objectMapper);
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(objectMapper.readValue(
                any(InputStream.class),
                eq(typeFactory.constructMapType(Map.class, typeFactory.constructType(String.class), typeFactory.constructCollectionLikeType(List.class, String.class)))))
                .thenReturn(getDuplicateTags());
        when(objectMapper.readValue(
                any(InputStream.class),
                eq(typeFactory.constructCollectionLikeType(List.class, String.class))))
                .thenReturn(getTags());
        usefulThingParser = PowerMockito.spy(new UsefulThingParser());
    }

    @Before
    public void setStaticData() throws Exception {
        mockStatic(PodcastFileUtils.class, FilenameUtils.class, AsciidocUtils.class, UrlUtils.class, Jsoup.class);
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(of(5));
        when(UrlUtils.checkUrlStatus(anyString())).thenReturn(200);
        when(UrlUtils.getGithubDescription(anyString())).thenReturn("Test Description");
        when(UrlUtils.findGithubLink(anyString())).thenReturn("http://github.com/Test/Hello");
    }

    @Before
    public void setJsoupConfiguration() throws Exception {
        Connection connectionMock = PowerMockito.mock(Connection.class);
        when(Jsoup.connect(Mockito.anyString())).thenReturn(connectionMock);
        when(connectionMock.get()).thenReturn(documentMock);
        when(documentMock.body()).thenReturn(element);
        when(documentMock.getElementById(Mockito.anyString())).thenReturn(element);
        when(element.getElementsMatchingOwnText(Mockito.any(Pattern.class))).thenReturn(new Elements(element));
        when(element.getElementsByTag(anyString())).thenReturn(new Elements(element));
        when(element.attributes().get(Mockito.anyString())).thenReturn("http://github.com/Test/Hello");
    }

    @Test
    public void parse_WithFilesAsciiDocFileAndGithubLink_ReturnUsefulThingsCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
        assertEquals(data.get(0).getName(), "Hello");
    }

    @Test
    public void parse_WithFilesAsciiDocFileAndDuplicateTags_ReturnUsefulThingsCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        BiPredicate<String, Element> test = (t, r) -> false;
        when(usefulThingParser, "filterTag").thenReturn(test);
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
        assertThat(data.get(0).getTags(), IsCollectionContaining.hasItem("Book"));
    }

    @Test
    public void parse_WithAsciiDocAndEmptyTagElement_ReturnUsefulThingsCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(documentMock.getElementById(Mockito.anyString())).thenReturn(null);
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
        assertThat(data.get(0).getTags(), IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithAsciiDocAndNotGithubLink_ReturnUsefulThingsCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(element.attributes().get(Mockito.anyString())).thenReturn("http://test.com");
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
        assertEquals(data.get(0).getDescription(), "Test Description");
    }

    @Test
    public void parse_WithAsciiDocAndNotGithubLinkAddWWWLink_ReturnUsefulThingsCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(element.attributes().get(Mockito.anyString())).thenReturn("http://www.test.com");
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
        assertEquals(data.get(0).getDescription(), "Test Description");
    }

    @Test
    public void parse_WithAsciiDocAndUrlUtilsThrowException_ReturnUsefulThingsCollectionWithNullDescription() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(element.attributes().get(Mockito.anyString())).thenReturn("http://www.test.com");
        when(UrlUtils.getGithubDescription(anyString())).thenThrow(new IOException());
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
        assertEquals(data.get(0).getDescription(), null);
    }

    @Test
    public void parse_WithExcludedUrl_ReturnEmptyUsefulThings() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(element.attributes().get(Mockito.anyString())).thenReturn("http://razbor-poletov.com");
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithGetGithubDescriptionThrownException_ReturnEmptyCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(UrlUtils.getGithubDescription(anyString())).thenThrow(new IOException());
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithErrorUrlResponse_ReturnEmptyCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(UrlUtils.checkUrlStatus(anyString())).thenReturn(404);
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithEmptyPodcastLinks_ReturnEmptyCollection() throws Exception {
        setFileConfiguration(Constants.ASCII_DOC);
        when(element.getElementsByTag(eq("a"))).thenReturn(null);
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithMarkdownFile_ReturnUsefulThingsCollection() throws Exception {
        setFileConfiguration(Constants.MARKDOWN_FORMAT);
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parse_WithMarkdownFileAndNullDocument_ReturnEmptyCollection() throws Exception {
        setFileConfiguration(Constants.MARKDOWN_FORMAT);
        when(Jsoup.parse(anyString())).thenReturn(null);
        List<UsefulThing> data = usefulThingParser.parse(file);
        assertThat(data, IsEmptyCollection.empty());
    }

    @Test(expected = RuntimeException.class)
    public void parse_WithJSoupThrownException_ThrowException() throws Exception {
        setFileConfiguration(Constants.MARKDOWN_FORMAT);
        when(MarkdownUtils.parseToHtml(any(File.class))).thenThrow(new IOException());
        usefulThingParser.parse(file);
    }

    @Test
    public void parse_WithInvalidPodcastId_ReturnEmptyCollection() {
        when(file.getName()).thenReturn("2012-02-16-episode-10.md");
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(of(-999));
        usefulThingParser.parse(file);
    }

    @Test
    public void parse_WithHTMLFile_ReturnUsefulThingsCollection() throws Exception {
        setFileConfiguration(Constants.HTML);
        List<UsefulThing> data = usefulThingParser.parse(Collections.singletonList(file));
        assertThat(data, IsCollectionWithSize.hasSize(1));
    }
    
    private void setFileConfiguration(String fileType) throws Exception {
        if (Constants.ASCII_DOC.equals(fileType)) {
            when(file.getName()).thenReturn("2012-02-16-episode-10.adoc");
            when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.ASCII_DOC);
            when(AsciidocUtils.parsePartById(any(File.class), anyString())).thenReturn(of(element));
        } else if (Constants.MARKDOWN_FORMAT.equals(fileType) || Constants.HTML.equals(fileType)) {
            if (Constants.MARKDOWN_FORMAT.equals(fileType)) {
                mockStatic(MarkdownUtils.class);
                when(MarkdownUtils.parseToHtml(any(File.class))).thenReturn("html");
                when(Jsoup.parse(anyString())).thenReturn(documentMock);
                when(file.getName()).thenReturn("2012-02-16-episode-10.md");
                when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.MARKDOWN_FORMAT);
            } else {
                when(file.getName()).thenReturn("2012-02-16-episode-10.html");
                when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.HTML);
                when(Jsoup.parse(any(File.class), anyString())).thenReturn(documentMock);
            }
            when(documentMock.html()).thenReturn("test Полезняшка test");
            Element elementMock = PowerMockito.mock(Element.class);
            when(documentMock.getElementsByTag(eq("li"))).thenReturn(new Elements(elementMock));
            when(elementMock.text()).thenReturn("test Полезняшка test");
            when(elementMock.getElementsByTag(eq("a"))).thenReturn(new Elements(element));
        }
    }

    private Map<String, List<String>> getDuplicateTags() {
        Map<String, List<String>> duplicateTags = new HashMap<>();
        duplicateTags.put("Book", Arrays.asList("Books", "book"));
        return duplicateTags;
    }

    private List<String> getTags() {
        return Arrays.asList("Book", "Test");
    }

}
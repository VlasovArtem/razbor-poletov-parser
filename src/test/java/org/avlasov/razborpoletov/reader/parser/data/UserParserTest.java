package org.avlasov.razborpoletov.reader.parser.data;

import org.apache.commons.io.FilenameUtils;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.twitter.TwitterAPI;
import org.avlasov.razborpoletov.reader.twitter.entity.TwitterUser;
import org.avlasov.razborpoletov.reader.utils.AsciidocUtils;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.avlasov.razborpoletov.reader.utils.MarkdownUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created By artemvlasov on 15/01/2018
 **/
@PrepareForTest({UserParser.class, PodcastFileUtils.class, AsciidocUtils.class, FilenameUtils.class, Jsoup.class, MarkdownUtils.class})
public class UserParserTest extends PowerMockitoTestCase {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Element element;
    @Mock
    private Document document;
    @Mock
    private File file;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private TwitterAPI twitterAPI;
    @InjectMocks
    private UserParser userParser;

    @Before
    public void setUp() throws Exception {
        when(twitterAPI.getTwitterUser(anyLong())).thenReturn(Optional.of(getTwitterUser().build()));
        when(twitterAPI.getTwitterUser(anyString())).thenReturn(Optional.of(getTwitterUser().build()));
        whenNew(RestTemplate.class).withAnyArguments().thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("test");
        mockStatic(PodcastFileUtils.class, AsciidocUtils.class, FilenameUtils.class, Jsoup.class, MarkdownUtils.class);
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(Optional.of(9));
        when(document.getElementsByTag(anyString())).thenReturn(new Elements(element));
        when(element.attr(eq("href"))).thenReturn("http://twitter.com/test");
        when(element.attributes().get(eq("href"))).thenReturn("http://twitter.com/test");
        when(Jsoup.parse(anyString())).thenReturn(document);
    }

    @Test
    public void parse_WithFile_ReturnUserCollection() {
        asciidocConfiguration();
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(1));
        assertThat(parse.get(0).getAppearanceEpisodeNumbers(), IsCollectionContaining.hasItem(9));
    }

    @Test
    public void parse_WithImageThrownError_ReturnUserWithEmptyImageUrl() {
        asciidocConfiguration();
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RestClientException("Test"));
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(1));
        assertEquals("", parse.get(0).getTwitterImgUrl());
    }

    @Test
    public void parse_WithZeroPodcastId_ReturnUserCollection() {
        asciidocConfiguration();
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(Optional.of(0));
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(2));
    }

    @Test
    public void parse_With145PodcastId_ReturnUserCollection() {
        asciidocConfiguration();
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(Optional.of(145));
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(3));
    }

    @Test
    public void parse_WithInvalidTwitterLink_ReturnEmptyCollection() {
        asciidocConfiguration();
        when(element.attr(eq("href"))).thenReturn("http://hello.com");
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithoutTwitterName_ReturnEmptyCollection() {
        asciidocConfiguration();
        when(element.attributes().get(eq("href"))).thenReturn("http://twitter.com");
        when(element.attr(eq("href"))).thenReturn("http://twitter.com");
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithHashTagTwitterLink_ReturnUserCollection() {
        asciidocConfiguration();
        when(element.attributes().get(eq("href"))).thenReturn("http://twitter.com/#!/test");
        when(element.attr(eq("href"))).thenReturn("http://twitter.com/#!/test");
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parse_WithDogTwitterLink_ReturnUserCollection() {
        asciidocConfiguration();
        when(element.attributes().get(eq("href"))).thenReturn("http://twitter.com/@test");
        when(element.attr(eq("href"))).thenReturn("http://twitter.com/@test");
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parse_WithNullDocument_ReturnEmptyCollection() {
        asciidocConfiguration();
        when(AsciidocUtils.parseTwitterPart(any(File.class))).thenReturn(Collections.singletonList(null));
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithMarkdownFile_ReturnUserCollection() throws Exception {
        when(MarkdownUtils.parseToHtml(any(File.class))).thenReturn("test");
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.MARKDOWN_FORMAT);
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parse_WithMarkdownFileThrownException_ReturnEmptyCollection() throws Exception {
        when(MarkdownUtils.parseToHtml(any(File.class))).thenThrow(new IOException());
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.MARKDOWN_FORMAT);
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithHTMLFile_ReturnUserCollection() throws Exception {
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.HTML);
        when(Jsoup.parse(any(File.class), anyString())).thenReturn(document);
        List<User> parse = userParser.parse(file);
        assertThat(parse, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parse_WithInvalidFileType_ReturnEmptyCollection() {
        when(FilenameUtils.getExtension(anyString())).thenReturn("");
        assertThat(userParser.parse(file), IsEmptyCollection.empty());
    }

    private void asciidocConfiguration() {
        when(AsciidocUtils.parseTwitterPart(any(File.class))).thenReturn(Collections.singletonList(document));
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.ASCII_DOC);
    }

    private TwitterUser.TwitterUserBuilder getTwitterUser() {
        return TwitterUser.builder()
                .description("Test description")
                .id(50)
                .location("Test Location")
                .name("Test Name")
                .profileImageUrl("http://twitter-img_normal")
                .screenName("Test ScreenName");
    }

}
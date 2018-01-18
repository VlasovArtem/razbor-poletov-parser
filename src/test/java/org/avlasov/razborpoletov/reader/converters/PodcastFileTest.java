package org.avlasov.razborpoletov.reader.converters;

import org.apache.commons.io.FilenameUtils;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.parser.statistic.PodcastStatisticParser;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.avlasov.razborpoletov.reader.utils.MarkdownUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.*;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 18/01/2018
 **/
@PrepareForTest({PodcastFile.class, PodcastFileUtils.class, FilenameUtils.class, Jsoup.class, MarkdownUtils.class})
public class PodcastFileTest extends PowerMockitoTestCase {

    @Mock
    private PodcastStatisticParser parser;
    @Mock
    private File file;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Document document;
    @Mock
    private FileReader fileReader;

    @Before
    public void setUp() throws Exception {
        mockStatic(PodcastFileUtils.class, FilenameUtils.class, Jsoup.class, MarkdownUtils.class);
        whenNew(File.class).withAnyArguments().thenReturn(file);
        when(PodcastFileUtils.getPodcastNumber(any(File.class))).thenReturn(Optional.of(10));
        whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
        BufferedReader bufferedReader = new BufferedReader(new StringReader("---\ntitle: test\ndate: hello\ntest\n---"));
        whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
        when(Jsoup.parse(anyString())).thenReturn(document);
        when(Jsoup.parse(anyString(), anyString())).thenReturn(document);
        when(document.getElementsByTag(anyString())).thenReturn(new Elements(document));
        when(document.attributes().get(eq("href"))).thenReturn("image.jpg");
    }

    @Test
    public void construct_withMarkdown() throws Exception {
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.MARKDOWN_FORMAT);
        when(MarkdownUtils.parseToHtml(any(File.class))).thenReturn("Test");
        new PodcastFile(file, parser);
    }

    @Test
    public void construct_withMarkdownAndImageNull() throws Exception {
        when(Jsoup.parse(anyString())).thenReturn(null);
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.MARKDOWN_FORMAT);
        when(MarkdownUtils.parseToHtml(any(File.class))).thenReturn("Test");
        new PodcastFile(file, parser);
    }

    @Test
    public void construct_withMarkdownAndEmptyBasicElements() throws Exception {
        BufferedReader bufferedReaderMock = mock(BufferedReader.class);
        whenNew(BufferedReader.class).withParameterTypes(Reader.class).withArguments(fileReader).thenReturn(bufferedReaderMock);
        when(bufferedReaderMock.readLine()).thenReturn(null);
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.MARKDOWN_FORMAT);
        when(MarkdownUtils.parseToHtml(any(File.class))).thenReturn("Test");
        new PodcastFile(file, parser);
    }

    @Test
    public void construct_WithHtml() throws Exception {
        when(FilenameUtils.getExtension(anyString())).thenReturn(Constants.HTML);
        new PodcastFile(file, parser);
    }
}
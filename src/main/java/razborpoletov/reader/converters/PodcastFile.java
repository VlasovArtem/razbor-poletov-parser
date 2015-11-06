package razborpoletov.reader.converters;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import razborpoletov.reader.parsers.StatisticParser;
import razborpoletov.reader.utils.MarkdownUtils;
import razborpoletov.reader.utils.PodcastFileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static razborpoletov.reader.utils.Constants.HTML;
import static razborpoletov.reader.utils.Constants.MARKDOWN_FORMAT;

/**
 * Created by artemvlasov on 04/06/15.
 */
public class PodcastFile {
    private final static String INTERMEDIATE_FILES_FOLDER = "./intermediate-adoc-files/";
    private final static String COMPLETE_FILES_WITHOUT_HTML_FOLDER = "./adoc-files/without-html/";
    private final static String COMPLETE_FILES_WITH_HTML_FOLDER = "./adoc-files/with-html/";
    private final static String TITLE_PATTERN = "title: .+";
    private final static String DATE_PATTERN = "date: .+";
    private final File originalFile;
    private final File intermediateConvertedFile;
    private final File convertedFileWithoutHtml;
    private final File convertedFileWithHtml;
    private final long mp3FileLength;
    private final long id;
    private final String outputFilename;
    private final String outputFile;
    private final String date;
    private final String title;
    private final String mp3Filename;
    private final String basicElement;
    private final String mp3Url;
    private final String imgUrl;
    private final String htmlAudioTag;
    private final String asciidocAudioTag;
    private final String htmlDonwloadTag;
    private final String asciidocDonwloadTag;
    private final String htmlImageTag;
    private final String asciidocImageTag;
    private String asciidocWithoutHtml;
    private String asciidocWithHtml;

    public PodcastFile(File originalFile) throws IOException, URISyntaxException {
        StatisticParser parser = new StatisticParser();
        this.originalFile = originalFile;
        mp3FileLength = parser.getFileSize(originalFile);
        id = PodcastFileUtils.getPodcastId(originalFile);
        outputFilename = String.format("%s.adoc", FilenameUtils.getBaseName(originalFile
                .getName()));
        outputFile = String.format("%s%s", INTERMEDIATE_FILES_FOLDER,
                outputFilename);
        convertedFileWithoutHtml = new File(String.format("%s%s", COMPLETE_FILES_WITHOUT_HTML_FOLDER,
                outputFilename));
        convertedFileWithoutHtml.createNewFile();
        convertedFileWithHtml = new File(String.format("%s%s", COMPLETE_FILES_WITH_HTML_FOLDER,
                outputFilename));
        convertedFileWithHtml.createNewFile();
        intermediateConvertedFile = new File(String.format("%s%s", INTERMEDIATE_FILES_FOLDER,
                outputFilename));
        List<String> basicElement = BasicElement.getBasicElement(originalFile);
        date = BasicElement.getPartContent(basicElement, DATE_PATTERN);
        title = BasicElement.getPartContent(basicElement, TITLE_PATTERN);
        mp3Filename = parser.getMp3Filename(originalFile);
        this.basicElement = TagsBuilder.basicAsciidocElementBuilder(id, mp3FileLength, title, date, mp3Filename);
        mp3Url = parser.getUrl(originalFile);
        imgUrl = getImageUrl();
        htmlAudioTag = TagsBuilder.audioTagHtmlBuilder(mp3Url);
        asciidocAudioTag = TagsBuilder.audioTagAsciidocBuilder(mp3Url);
        htmlDonwloadTag = TagsBuilder.downloadTagHtmlBuilder(mp3Url);
        asciidocDonwloadTag = TagsBuilder.downloadTagAsciidocBuilder(mp3Url);
        if(imgUrl != null) {
            htmlImageTag = TagsBuilder.imageTagHtmlBuilder(imgUrl);
            asciidocImageTag = TagsBuilder.imageTagAsciidocBuilder(imgUrl);
        } else {
            htmlImageTag = null;
            asciidocImageTag = null;
        }
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public File getIntermediateConvertedFile() {
        return intermediateConvertedFile;
    }

    public File getConvertedFileWithoutHtml() {
        return convertedFileWithoutHtml;
    }

    public File getConvertedFileWithHtml() {
        return convertedFileWithHtml;
    }

    public String getBasicElement() {
        return basicElement;
    }

    public String getHtmlAudioTag() {
        return htmlAudioTag;
    }

    public String getAsciidocAudioTag() {
        return asciidocAudioTag;
    }

    public String getHtmlDonwloadTag() {
        return htmlDonwloadTag;
    }

    public String getAsciidocDonwloadTag() {
        return asciidocDonwloadTag;
    }

    public String getHtmlImageTag() {
        return htmlImageTag;
    }

    public String getAsciidocImageTag() {
        return asciidocImageTag;
    }

    public String getAsciidocWithoutHtml() {
        return asciidocWithoutHtml;
    }

    public void setAsciidocWithoutHtml(String asciidocWithoutHtml) {
        this.asciidocWithoutHtml = asciidocWithoutHtml;
    }

    public String getAsciidocWithHtml() {
        return asciidocWithHtml;
    }

    public void setAsciidocWithHtml(String asciidocWithHtml) {
        this.asciidocWithHtml = asciidocWithHtml;
    }

    public String getOutputFile() {
        return outputFile;
    }

    private static class BasicElement {
        private static List<String> getBasicElement(File file) throws IOException {
            List<String> basicElementParts = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                int countBasicElement = 0;
                for(String line; (line = br.readLine()) != null; ) {
                    if(line.equals("---")) {
                        countBasicElement++;
                    }
                    if(countBasicElement == 1) {
                        basicElementParts.add(line);
                    } else if(countBasicElement > 1) {
                        break;
                    }
                }
            }
            if(basicElementParts.isEmpty()) {
                System.out.println(String.format("File %s has no basicElement", file.getName()));
            }
            return basicElementParts;
        }
        private static String getPartContent(List<String> basicElementParts, String pattern) {
            String part = basicElementParts
                    .stream()
                    .filter(p -> Pattern.compile(pattern).matcher(p).matches())
                    .findFirst()
                    .get();
            if(part == null) {
                part = "none";
            } else if(TITLE_PATTERN.equals(pattern)) {
                part = part.split(" ", 2)[1].replaceAll("\"", "");
            } else if(DATE_PATTERN.equals(pattern)) {
                part = part.split(" ", 2)[1];
            } else {
                part = "undefined";
            }
            return part;
        }
    }
    private String getImageUrl() throws IOException {
        Document document = null;
        FilenameUtils.getExtension(originalFile.getAbsolutePath());
        switch (FilenameUtils.getExtension(originalFile.getAbsolutePath())) {
            case MARKDOWN_FORMAT:
                document =  Jsoup.parse(MarkdownUtils.parseToHtml(originalFile));
                break;
            case HTML:
                document = Jsoup.parse(originalFile, "UTF-8");
                break;
        }
        if(document != null) {
            Elements elements = document.getElementsByTag("a");
            Optional<Element> imgUrl = elements.stream().filter(element -> element.attributes().get("href").contains("" +
                    ".jpg")).findFirst();
            if(imgUrl.isPresent()) {
                return imgUrl.get().attributes().get("href").replaceAll("\\s", "");
            }
        }
        return null;
    }
}

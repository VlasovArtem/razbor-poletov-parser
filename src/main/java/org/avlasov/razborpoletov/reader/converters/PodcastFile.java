package org.avlasov.razborpoletov.reader.converters;

import org.apache.commons.io.FilenameUtils;
import org.avlasov.razborpoletov.reader.parser.statistic.PodcastStatisticParser;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.avlasov.razborpoletov.reader.utils.MarkdownUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 04/06/15.
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
    private final PodcastStatisticParser parser;

    @Autowired
    public PodcastFile(File originalFile, PodcastStatisticParser parser) throws IOException, URISyntaxException {
        this.parser = parser;
        mp3Url = this.parser.getUrl(originalFile);
        this.originalFile = originalFile;
        mp3FileLength = this.parser.getMP3FileLength(mp3Url);
        id = PodcastFileUtils.getPodcastNumber(originalFile).get();
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
        mp3Filename = this.parser.getMp3Filename(mp3Url);
        this.basicElement = TagsBuilder.basicAsciidocElementBuilder(id, mp3FileLength, title, date, mp3Filename);
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
            Optional<String> partOptional = basicElementParts
                    .stream()
                    .filter(p -> Pattern.compile(pattern).matcher(p).matches())
                    .findFirst();
            if (partOptional.isPresent()) {
                if(TITLE_PATTERN.equals(pattern)) {
                    return partOptional.get().split(" ", 2)[1].replaceAll("\"", "");
                } else if(DATE_PATTERN.equals(pattern)) {
                    return partOptional.get().split(" ", 2)[1];
                } else {
                    return "undefined";
                }
            } else {
                return "none";
            }
        }
    }

    private String getImageUrl() throws IOException {
        Document document = null;
        FilenameUtils.getExtension(originalFile.getAbsolutePath());
        switch (FilenameUtils.getExtension(originalFile.getAbsolutePath())) {
            case Constants.MARKDOWN_FORMAT:
                document =  Jsoup.parse(MarkdownUtils.parseToHtml(originalFile));
                break;
            case Constants.HTML:
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

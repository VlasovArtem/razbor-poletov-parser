package org.avlasov.razborpoletov.reader.parser.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.exception.PodcastLinkParseException;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 04/07/2017.
 */
@Component
public class LinkParser {

    private static final Logger LOGGER = LogManager.getLogger(LinkParser.class);
    private FileParser fileParser;

    @Autowired
    public LinkParser(FileParser fileParser) {
        this.fileParser = fileParser;
    }

    public Optional<PodcastLink> parsePodcastLink(int podcastNumber) {
        return parsePodcastLinks(new int[] {podcastNumber}).stream().findFirst();
    }

    public List<PodcastLink> parsePodcastLinks(int... podcastNumbers) {
        try {
            Document document = Jsoup.connect("http://razbor-poletov.com/blog/archives/").get();
            if (Objects.isNull(podcastNumbers) || podcastNumbers.length == 0) {
                podcastNumbers = PodcastFileUtils.getPodcastsIdArray(fileParser.getPodcastsFiles());
            }
            return Arrays
                    .stream(podcastNumbers)
                    .boxed()
                    .map(parseLink(document))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("HTML could not be parsed from 'http://razbor-poletov.com/blog/archives/'");
            return Collections.emptyList();
        }
    }

    public List<PodcastLink> parseAllPodcastLinks() {
        return parsePodcastLinks(null);
    }

    private Function<Integer, PodcastLink> parseLink(Document document) {
        return podcastNumber -> {
            Element elementById = document.getElementById("blog-archives");
            if (Objects.nonNull(elementById)) {
                Optional<String> link = elementById
                        .getElementsByTag("article")
                        .stream()
                        .map(element ->
                                element.getElementsByTag("a").stream()
                                        .map(data -> data.attr("href"))
                                        .filter(hrefLink -> hrefLink.matches(String.format("/\\d{4}/\\d{2}/episode(-)?%s.*\\.html", podcastNumber)))
                                        .findFirst()
                                        .orElse(""))
                        .filter(s -> !s.isEmpty())
                        .findFirst();
                if (link.isPresent()) {
                    return new PodcastLink(podcastNumber, String.format("http://razbor-poletov.com%s", link.get()));
                } else {
                    LOGGER.warn("Link for podcast number {} is not found", podcastNumber);
                }
            } else {
                throw new PodcastLinkParseException("blog-archives id is not found");
            }
            return null;
        };
    }

}

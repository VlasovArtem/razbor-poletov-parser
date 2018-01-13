package org.avlasov.razborpoletov.reader.parser.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.exception.PodcastLinkParseException;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by artemvlasov on 04/07/2017.
 */
@Component
public class LinkParser implements Parser<PodcastLink> {

    private static final Logger LOGGER = LogManager.getLogger(LinkParser.class);
    private PodcastFolderUtils podcastFolderUtils;

    @Autowired
    public LinkParser(PodcastFolderUtils podcastFolderUtils) {
        this.podcastFolderUtils = podcastFolderUtils;
    }

    public List<PodcastLink> parseAllPodcastLinks() {
        return parsePodcastLinks((int[]) null);
    }

    @Override
    public List<PodcastLink> parse(List<File> files) {
        int[] podcastsIdArray = PodcastFileUtils.getPodcastsIdArray(files);
        if (Objects.nonNull(podcastsIdArray) && podcastsIdArray.length > 0) {
            return parsePodcastLinks(podcastsIdArray);
        } else {
            LOGGER.warn("Podcast array is empty or null. Empty list will be returned.");
        }
        return Collections.emptyList();
    }

    @Override
    public List<PodcastLink> parse(File file) {
        Optional<Integer> podcastNumber = PodcastFileUtils.getPodcastNumber(file);
        if (podcastNumber.isPresent()) {
            return podcastNumber
                    .map(this::parsePodcastLink)
                    .filter(Optional::isPresent)
                    .map(link -> Collections.singletonList(link.get()))
                    .orElseGet(Collections::emptyList);
        } else {
            LOGGER.warn("Podcast number for the file {} is not found.");
        }
        return Collections.emptyList();
    }

    public List<PodcastLink> parsePodcastLinks(int... podcastNumbers) {
        try {
            Document document = Jsoup.connect("http://razbor-poletov.com/blog/archives/").get();
            if (Objects.isNull(podcastNumbers) || podcastNumbers.length == 0) {
                podcastNumbers = PodcastFileUtils.getPodcastsIdArray(podcastFolderUtils.getAllPodcastFiles());
            }
            LOGGER.info("Start parsing podcast links from the podcast numbers: {}.", IntStream.of(podcastNumbers).mapToObj(String::valueOf).collect(Collectors.joining(", ")));
            return Arrays
                    .stream(podcastNumbers)
                    .boxed()
                    .map(parseLink(document))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(PodcastLink::getPodcastNumber))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("HTML could not be parsed from 'http://razbor-poletov.com/blog/archives/'");
            LOGGER.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<PodcastLink> parsePodcastLink(int podcastNumber) {
        return parsePodcastLinks(podcastNumber).stream().findFirst();
    }

    private Function<Integer, PodcastLink> parseLink(Document document) {
        return podcastNumber -> {
            Element elementById = document.getElementById("blog-archives");
            if (Objects.nonNull(elementById)) {
                List<String> collect = elementById
                        .getElementsByTag("article")
                        .stream()
                        .map(element ->
                                element.getElementsByTag("a").stream()
                                        .map(data -> data.attr("href"))
                                        .filter(hrefLink -> hrefLink.matches(String.format("/\\d{4}/\\d{2}/episode(-)?%s(-\\w+)*\\.html", podcastNumber)))
                                        .findFirst()
                                        .orElse(""))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                Optional<String> link = Optional.empty();
                if (collect.size() > 1) {
                    Optional<String> first = collect
                            .stream()
                            .filter(linkData -> linkData.matches(String.format("/\\d{4}/\\d{2}/episode(-)?%s\\.html", podcastNumber)))
                            .findFirst();
                    if (!first.isPresent()) {
                        link = collect.stream().findFirst();
                    } else {
                        link = first;
                    }
                } else if (!collect.isEmpty()) {
                    link = Optional.ofNullable(collect.get(0));
                }
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

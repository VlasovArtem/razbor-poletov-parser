package org.avlasov.razborpoletov.reader.parser.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.info.UsefulThing;
import org.avlasov.razborpoletov.reader.utils.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 20/05/15.
 */
@Component
public class UsefulThingParser implements Parser<UsefulThing> {

    private final static Logger LOGGER = LogManager.getLogger(UsefulThingParser.class);
    private List<String> tags;
    private Map<String, List<String>> duplicateTags;

    public UsefulThingParser() throws IOException {
        localParseTags();
    }

    @Override
    public List<UsefulThing> parse(List<File> files) {
        List<UsefulThing> allUsefulThings = Optional.ofNullable(files)
                .orElseGet(Collections::emptyList)
                .stream()
                .flatMap(file -> {
                    List<UsefulThing> usefulThings = parse(file);
                    LOGGER.info("{} useful things was parsed from file {}.", usefulThings.size(), file.getName());
                    return usefulThings.stream();
                })
                .collect(Collectors.toList());
        LOGGER.info("{} useful things was parsed from files {}.", allUsefulThings.size(), files.size());
        return allUsefulThings;
    }

    @Override
    public List<UsefulThing> parse(File file) {
        try {
            Integer podcastId = PodcastFileUtils.getPodcastNumber(file).orElse(-999);
             if (Pattern.matches(PODCAST_FILE_PATTERN, file.getName()) && podcastId >= 0) {
                LOGGER.info("Start collecting useful things for the podcast {}.", podcastId);
                switch (FilenameUtils.getExtension(file.getName())) {
                    case Constants.ASCII_DOC:
                        return AsciidocUtils.parsePartById(file, "Полезняшки")
                                .map(doc -> parse(doc.getElementsByTag("a"), podcastId))
                                .orElseGet(Collections::emptyList);
                    case Constants.MARKDOWN_FORMAT:
                        return parse(Jsoup.parse(MarkdownUtils.parseToHtml(file)), podcastId);
                    case Constants.HTML:
                        return parse(Jsoup.parse(file, "UTF-8"), podcastId);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return Collections.emptyList();
    }

    private List<UsefulThing> parse(Document document, long podcastId) {
        if (Objects.nonNull(document)) {
            String html = document.html();
            Elements elements = null;
            if (html.contains("Полезняшка") || html.contains("Полезняшки")) {
                for (Element element : document.getElementsByTag("li")) {
                    if (element.text().contains("Полезняшка") || element.text().contains("Полезняшки")) {
                        elements = element.getElementsByTag("a");
                    }
                }
            }
            return parse(elements, podcastId);
        }
        return Collections.emptyList();
    }

    private List<UsefulThing> parse(Elements elements, long podcastId) {
        if (Objects.nonNull(elements) && podcastId >= 0) {
            List<UsefulThing> usefulThings = new ArrayList<>();
            for (Element element : elements) {
                String url = element.attributes().get("href");
                int responseCode = UrlUtils.checkUrlStatus(url);
                if ("http://razbor-poletov.com".equals(url)) {
                    return usefulThings;
                }
                if (!Pattern.matches("(^(4|5)([0-9]{2})$)|0", String.valueOf(responseCode))
                        && !url.contains("razbor-poletov")) {
                    try {
                        usefulThings.add(setUsefulThingContent(url, podcastId));
                    } catch (IOException | URISyntaxException e) {
                        LOGGER.error(e);
                    }
                } else {
                    LOGGER.info("Url {} return error with status {}", url, responseCode);
                }
            }
            return usefulThings;
        }
        return Collections.emptyList();
    }

    /**
     * Parse popular tags from the file in classpath
     *
     * @throws IOException
     */
    private void localParseTags() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        duplicateTags = objectMapper.readValue(getClass().getResourceAsStream(Constants.DUPLICATE_TAGS),
                typeFactory.constructMapType(Map.class, typeFactory.constructType(String.class), typeFactory.constructCollectionLikeType(List.class, String.class)));
        tags = objectMapper.readValue(getClass().getResourceAsStream(Constants.TAGS),
                typeFactory.constructCollectionLikeType(List.class, String.class));
    }

    /**
     * Set data to entity UsefulThing name, ulr, tags and description. If url is not github url it will create name
     * from the url and get description of the project from github.
     *
     * @param url
     * @throws IOException
     * @throws URISyntaxException
     */
    private UsefulThing setUsefulThingContent(String url, long podcastId) throws IOException, URISyntaxException {
        String description = null;
        String name = null;
        if (url.contains("github")) {
            if (url.lastIndexOf("/") != url.length() - 1 && !url.substring(url.lastIndexOf
                    ("/"), url.length()).contains("github")) {
                String substringName = url.substring(url.lastIndexOf("/") + 1, url.length());
                if (substringName.length() != 0) {
                    name = substringName;
                    description = UrlUtils.getGithubDescription(url);
                }
            }
        }
        if (name == null) {
            if (url.contains("http") || url.contains("https") || url.contains("www")) {
                if (url.contains("www.")) {
                    name = url.substring(url.indexOf("www.") + "www.".length())
                            .split("\\.")[0];
                } else {
                    name = url.substring(url.indexOf("://") + "://".length())
                            .split("\\.")[0];
                }
            }
            try {
                String githubUrl = UrlUtils.findGithubLink(url);
                if (githubUrl != null) {
                    description = UrlUtils.getGithubDescription(githubUrl);
                }
            } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
                LOGGER.warn(e.getMessage());
            }

        }
        return new UsefulThing(url, StringUtils.capitalize(name), parseTags(url), false, podcastId, description);
    }

    /**
     * Parse tags from the web site content. Method check status of the link if it's available. If link is github
     * link, it will get special part of HTML, that contains useful information about link.
     * This method will return top 5 common tags.
     *
     * @param url
     * @return List of tags
     * @throws IOException
     */
    private List<String> parseTags(String url) throws IOException {
        List<String> tags = Collections.emptyList();
        if (!Pattern.matches("(^(4|5)([0-9]{2})$)|0", String.valueOf(UrlUtils.checkUrlStatus(url)))) {
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            final Element element = Pattern.compile("https?://github.com/.+/.+").matcher(url).matches()
                    ? document.getElementById("readme")
                    : document.body();
            if (element != null) {
                tags = this.tags.stream()
                        .filter(tag ->
                                filterTag()
                                        .or(filterDuplicateTags())
                                        .test(tag, element))
                        .distinct()
                        .sorted(Comparator.comparing(tag ->
                                -element.getElementsMatchingOwnText(Pattern.compile("^(" + tag + ")$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)).size()))
                        .limit(5)
                        .collect(Collectors.toList());
            } else {
                LOGGER.warn("{} url not contains html body", url);
            }
        }
        return tags;
    }

    /**
     * Filter tag by matching element by pattern '^(tag)$' with flags {@link Pattern#CASE_INSENSITIVE} and {@link Pattern#MULTILINE}
     *
     * @return {@link true} if Element contains tag, otherwise {@link false}
     */
    private BiPredicate<String, Element> filterTag() {
        return (tag, element) -> element.getElementsMatchingOwnText(Pattern.compile("^(" + tag + ")$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)).size() > 0;
    }

    /**
     * Filter tag by duplicate tag, that is matching element own text
     *
     * @return {@link true} if one of duplicate tags is matches elements own text, otherwise {@link fasle}
     */
    private BiPredicate<String, Element> filterDuplicateTags() {
        return (tag, element) ->
                duplicateTags.getOrDefault(tag, Collections.emptyList())
                        .stream()
                        .anyMatch(dt -> element.getElementsMatchingOwnText(Pattern.compile(dt, Pattern.CASE_INSENSITIVE)).size() > 0);
    }

}

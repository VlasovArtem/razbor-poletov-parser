package org.avlasov.razborpoletov.reader.parser.data;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.info.Conference;
import org.avlasov.razborpoletov.reader.utils.AsciidocUtils;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.avlasov.razborpoletov.reader.utils.MarkdownUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 20/05/15.
 */
@Component
public class ConferenceParser implements Parser<Conference> {

    private final static Logger LOGGER = LogManager.getLogger(ConferenceParser.class);
    private final List<String> ignoredConferenceUrl = Arrays.asList("instagram", "youtube");
    private Set<String> uniqueConferenceUrl = new HashSet<>();

    @Override
    public List<Conference> parse(List<File> files) {
        List<Conference> conferences = Optional.ofNullable(files)
                .orElseGet(Collections::emptyList)
                .stream()
                .flatMap(file -> {
                    List<Conference> parse = parse(file);
                    LOGGER.info("{} conferences was parsed from file {}.", parse.size(), file.getName());
                    return parse.stream();
                })
                .collect(Collectors.toList());
        LOGGER.info("{} conferences was parsed from all {} files.", conferences.size(), files.size());
        return conferences;
    }

    public List<Conference> parse(File file) {
        if (Pattern.matches(PODCAST_FILE_PATTERN, file.getName())) {
            try {
                switch (FilenameUtils.getExtension(file.getName())) {
                    case Constants.ASCII_DOC:
                        return AsciidocUtils.parsePartById(file, "_Конференции")
                                .map(conferencesDocument -> parse(conferencesDocument.getElementsByTag("a")))
                                .orElseGet(Collections::emptyList);
                    case Constants.MARKDOWN_FORMAT:
                        return parse(Jsoup.parse(MarkdownUtils.parseToHtml(file)));
                    case Constants.HTML:
                        return parse(Jsoup.parse(file, "UTF-8"));
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

    private List<Conference> parse(Document document) {
        return document != null ? parse(document.getElementsByTag("a")) : Collections.emptyList();
    }

    private List<Conference> parse(Elements elements) {
        List<Conference> conferences = new ArrayList<>();
        for (Element element : elements) {
            Conference conference = new Conference();
            Optional<TextNode> first = element.textNodes().stream().findFirst();
            if (first.isPresent()) {
                String name = first.get().getWholeText();
                String url = element.attributes().get("href");
                if (!uniqueConferenceUrl.contains(url) && ignoredConferenceUrl.stream().noneMatch(url::contains)) {
                    uniqueConferenceUrl.add(url);
                    if (name.equals(url)) {
                        if (name.contains("http") || name.contains("https") || name.contains("www")) {
                            if (name.contains("www.")) {
                                name = name.substring(name.indexOf("www.") + "www.".length())
                                        .split("\\.")[0];
                            } else {
                                name = name.substring(name.indexOf("://") + "://".length())
                                        .split("\\.")[0];
                            }
                        }
                    }
                    if (!url.contains("razbor-poletov")) {
                        conference.setName(name);
                        conference.setWebsite(url);
                        conferences.add(conference);
                    }
                }
            }
        }
        return conferences;
    }
}

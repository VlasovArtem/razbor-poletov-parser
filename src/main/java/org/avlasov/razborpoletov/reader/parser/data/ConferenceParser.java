package org.avlasov.razborpoletov.reader.parser.data;

import org.apache.commons.io.FilenameUtils;
import org.avlasov.razborpoletov.reader.entity.info.Conference;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.avlasov.razborpoletov.reader.utils.AsciidocUtils;
import org.avlasov.razborpoletov.reader.utils.MarkdownUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 20/05/15.
 */
@Component
public class ConferenceParser {
    private final List<String> ignoredConferenceUrl = Arrays.asList("instagram", "youtube");
    private Set<String> uniqueConferenceUrl = new HashSet<>();

    public List<Conference> parse(List<File> files, boolean asciidocOnly) throws IOException,
            URISyntaxException {
        List<Conference> conferences = new ArrayList<>();
        for (File file : files) {
            if(Pattern.matches("20([0-9]{2}-){3}episode-[0-9].+", file.getName())) {
                if(asciidocOnly) {
                    conferences.addAll(parseAsciidoc(file));
                } else {
                    List<Conference> parsedConferences = new ArrayList<>();
                    switch (FilenameUtils.getExtension(file.getName())) {
                        case Constants.ASCII_DOC:
                            AsciidocUtils.parsePartById(file, "_Конференции")
                                    .ifPresent(conferencesDocument -> parse(conferencesDocument.getElementsByTag("a")));
                            break;
                        case Constants.MARKDOWN_FORMAT:
                            parsedConferences = parse(Jsoup.parse(MarkdownUtils.parseToHtml(file)));
                            break;
                        case Constants.HTML:
                            parsedConferences = parse(file);
                            break;
                    }
                    if(parsedConferences != null) {
                        conferences.addAll(parsedConferences);
                    }
                }
            }
        }
        return conferences;
    }

    public List<Conference> parse(File file) throws IOException, URISyntaxException {
        Pattern pattern = Pattern.compile(".+(?=(a*(sc)?i*(doc)?d?))\\.a*(sc)?i*(doc)?d?");
        Matcher matcher = pattern.matcher(file.getName());
        if(matcher.matches()) {
            return parseAsciidoc(file);
        }
        return parse(Jsoup.parse(file, "UTF-8"));
    }

    private List<Conference> parseAsciidoc(File file) throws IOException, URISyntaxException {
        return AsciidocUtils.parsePartById(file, "_Конференции")
                .map(conferenceDocument -> parse(conferenceDocument.getElementsByTag("a")))
                .orElse(Collections.emptyList());
    }

    private List<Conference> parse(Document document) {
        return document != null ? parse(document.getElementsByTag("a")) : Collections.emptyList();
    }

    private List<Conference> parse(Elements elements) {
        List<Conference> conferences = new ArrayList<>();
        for(Element element : elements) {
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

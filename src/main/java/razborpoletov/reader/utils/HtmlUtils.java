package razborpoletov.reader.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import razborpoletov.reader.entity.Conference;
import razborpoletov.reader.entity.Twitter;
import razborpoletov.reader.entity.UsefulThing;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 26/04/15.
 */
public class HtmlUtils {
    private final List<String> ignoredConferenceUrl = Arrays.asList("instagram", "youtube");
    private Set<String> uniqueConferenceUrl = new HashSet<>();
    private static final Logger LOG = LoggerFactory.getLogger(HtmlUtils.class);
    private final List<String> protocols = Arrays.asList("http://", "https://");
    private List<String> tags;
    private Map<String, List<String>> duplicateTags;
    private List<String> ignoredUrls;

    public HtmlUtils() throws IOException {
        tags = new ArrayList<>();
        duplicateTags = new HashMap<>();
        ignoredUrls = new ArrayList<>();
        localParseTags();
    }

    public List<Twitter> parseTwitter(File file) throws IOException {
        return parseTwitter(Jsoup.parse(file, "UTF-8"));
    }
    public List<UsefulThing> parseUsefulStuff(File file) throws IOException, URISyntaxException {
        return parseUsefulStuff(Jsoup.parse(file, "UTF-8").html());
    }
    public List<UsefulThing> parseUsefulStuff(String html) throws IOException, URISyntaxException {
        Elements elements = null;
        if(html.contains("Полезняшка") || html.contains("Полезняшки")) {
            Document document = Jsoup.parse(html);
            for(Element element : document.getElementsByTag("li")) {
                if(element.text().contains("Полезняшка") || element.text().contains("Полезняшки")) {
                    elements = element.getElementsByTag("a");
                }
            }
        }
        return elements != null ? parseUsefulStuff(elements) : null;
    }
    public List<UsefulThing> parseUsefulStuffsAsciidoc(Document document) throws IOException, URISyntaxException {
        return document != null ? parseUsefulStuff(document.getElementsByTag("a")) : null;
    }
    public List<Conference> parseConferencesAsciidoc(Document document) {
        return document != null ? parseConference(document.getElementsByTag("a")) : null;
    }
    public List<Twitter> parseTwitter(Document document) {
        Elements elements = document.getElementsByTag("a");
        List<Element> filteredElements = elements.stream().filter(element -> element.attr("href").contains
                ("twitter.com")
                &&
                element.textNodes()
                        .stream().allMatch(node -> node.getWholeText().contains("@"))).collect(Collectors.toList());
        List<Twitter> twitters = new ArrayList<>(filteredElements.size());
        for(Element el : filteredElements) {
            String account = el.textNodes().stream().findFirst().get().getWholeText();
            String accountUrl = el.attributes().get("href");
            if(accountUrl != null && accountUrl.contains("/#!")) {
                String[] splitedAccountUrl = accountUrl.split("/#!");
                accountUrl = splitedAccountUrl[0] + splitedAccountUrl[1];
            }
            if(accountUrl != null && accountUrl.contains("/@")) {
                String[] splitedAccountUrl = accountUrl.split("/@");
                accountUrl = splitedAccountUrl[0] + "/" + splitedAccountUrl[1];
            }
            if(accountUrl != null && !accountUrl.contains("https")) {
                accountUrl = accountUrl.replace("http", "https");
            }
            twitters.add(new Twitter(account, accountUrl));
        }
        return twitters;
    }
    private List<UsefulThing> parseUsefulStuff(Elements elements) {
        List<UsefulThing> usefulStuffs = new ArrayList<>();
        for(Element element : elements) {
            UsefulThing usefulStuff = new UsefulThing();
            String url = element.attributes().get("href");
            int responseCode = 0;
            responseCode = checkUrlStatus(url);
            if(!Pattern.matches("(^(4|5)([0-9]{2})$)|0", String.valueOf(responseCode)) && !url.contains("razbor-poletov")) {
                try {
                    setUsefulThingContent(usefulStuff, url);
                    usefulStuffs.add(usefulStuff);
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    LOG.info(e.getMessage());
                    continue;
                }
            } else {
                LOG.info("Url {} return error with status {}", url, responseCode);
            }
        }
        return usefulStuffs;
    }

    private List<Conference> parseConference(Elements elements) {
        List<Conference> conferences = new ArrayList<>();
        for(Element element : elements) {
            Conference conference = new Conference();
            String name = element.textNodes().stream().findFirst().get().getWholeText();
            String url = element.attributes().get("href");
            if (!uniqueConferenceUrl.contains(url) && !ignoredConferenceUrl.stream().anyMatch(url::contains)) {
                uniqueConferenceUrl.add(url);
                if (name.equals(url)) {
                    if (name.contains("http") || name.contains("https") || name.contains("www")) {
                        if (name.contains("www.")) {
                            name = name.substring(name.indexOf("www.") + "www." .length())
                                    .split("\\.")[0];
                        } else {
                            name = name.substring(name.indexOf("://") + "://" .length())
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
        return conferences;
    }

    /**
     * Parse tags from the web site content.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private List<String> parseTags(String url) throws IOException, URISyntaxException {
        List<String> tags = null;
        if(!Pattern.matches("(^(4|5)([0-9]{2})$)|0", String.valueOf(checkUrlStatus(url)))) {
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            final Element element = Pattern.compile("https?://github.com/.+/.+").matcher(url).matches() ? document
                    .getElementById("readme") : document.body();
            if(element != null) {
                tags = this.tags.stream()
                        .filter(tag ->
                                element.getElementsMatchingOwnText(Pattern.compile(tag, Pattern
                                        .CASE_INSENSITIVE)).size() > 0 || (duplicateTags.containsKey(tag) && duplicateTags
                                        .get(tag).stream().anyMatch(dt -> element.getElementsMatchingOwnText(Pattern.compile
                                                (dt, Pattern.CASE_INSENSITIVE)).size() > 0)))
                        .distinct()
                        .sorted(Comparator.comparing(tag -> -element.getElementsMatchingOwnText(tag).size()))
                        .limit(5)
                        .collect(Collectors.toList());
            } else {
                LOG.warn("{} url not contains html body");
            }

        }
        return tags;
    }

    /**
     * Parse popular tags from the file in classpath
     * @throws IOException
     */
    private void localParseTags() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        duplicateTags = mapper.readValue(new File("duplicateTags.json"), new TypeReference<Map<String, List<String>>>() {});
        tags = mapper.readValue(new File("tags.json"), new TypeReference<List<String>>() {});
        ignoredUrls = Arrays.asList("http://www.latencytop.org/", "http://tobacco.noroutine.me", "http://www.fossil-scm.org/index.html/doc/tip/www/quickstart.wiki");
    }

    /**
     * Set data to entity UsefulThing name, ulr, tags and description. If url is not github url it will create name
     * from the url and get description of the project from github.
     * @param usefulThing
     * @param url
     * @throws IOException
     * @throws URISyntaxException
     */
    private void setUsefulThingContent(UsefulThing usefulThing, String url) throws IOException, URISyntaxException {
        String name = null;
        if(url.contains("github")) {
            if(url.lastIndexOf("/") != url.length() - 1 && !url.substring(url.lastIndexOf
                    ("/"), url.length()).contains("github")) {
                String substringName = url.substring(url.lastIndexOf("/") + 1, url.length());
                if (substringName.length() != 0) {
                    name = substringName;
                    usefulThing.setDescription(getGithubDescription(url));
                }
            }
        }
        if(name == null) {
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
                String githubUrl = findGithubLink(url);
                if(githubUrl != null) {
                    usefulThing.setDescription(getGithubDescription(githubUrl));
                }
            } catch (IOException | URISyntaxException | UnsupportedOperationException e ) {
                LOG.warn(e.getMessage());
            }

        }
        usefulThing.setTags(parseTags(url));
        usefulThing.setProvider(name);
        usefulThing.setLink(url);
    }

    /**
     * Get description of the project form the github page.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private String getGithubDescription(@javax.validation.constraints.Pattern(regexp = "https?://github.com/.+/.+", message = "Incorrect github url") String url) throws IOException, URISyntaxException {
        String description = null;
        URLConnection huc = getURL(url).openConnection();
        Document document = Jsoup.parse(huc.getInputStream(), null, url.toString());
        Elements elements = document.getElementsByClass("repository-description");
        if (elements.size() != 0) {
            description = elements.stream().findFirst().get().textNodes().stream().findFirst().get().getWholeText()
                    .trim();
        }
        return description;
    }

    /**
     * Search for the github link on the non github urls.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private String findGithubLink(String url) throws IOException, URISyntaxException {
        URLConnection huc = getURL(url).openConnection();
        Document document = Jsoup.parse(huc.getInputStream(), null, url.toString());
        Pattern pattern = Pattern.compile("(?:https?://github.com(/([^/])+){2})");
        Set<String> githubUniqueUrl = document.getElementsByTag("a").stream()
                .filter(element -> element.attr("href").matches("https?://github.com.+"))
                .map(el -> el.attr("href"))
                .distinct()
                .map(uniqueUrl -> {
                    final Matcher m = pattern.matcher(uniqueUrl);
                    return m.find() ? m.group(0) : null;
                })
                .filter(uniqueUrl -> uniqueUrl != null)
                .map(uniqueUrl -> {
                    if (Pattern.compile("(?=http://).+").matcher(uniqueUrl).find()) {
                        return uniqueUrl.replaceFirst("http://", "https://");
                    } else {
                        return uniqueUrl;
                    }
                })
                .collect(Collectors.toSet());
        if(githubUniqueUrl.isEmpty()) {
            LOG.info("{} url has no github links", url);
        } else if(githubUniqueUrl.size() > 1) {
            LOG.warn("{} url contains more than one github links: {}", url, githubUniqueUrl.toString());
        } else if(Pattern.matches("(^(4|5)([0-9]{2})$)|0", String.valueOf(checkUrlStatus
                (githubUniqueUrl.stream().findFirst().get())))) {
            LOG.warn("{} url github link is not available: {}", url, githubUniqueUrl.stream().findFirst().get());
        } else {
            return githubUniqueUrl.stream().findFirst().get();
        }
        return null;
    }

    /**
     * Check status of the url.
     * @param url
     * @return Responce code of the url
     * @throws URISyntaxException
     * @throws IOException
     */
    private int checkUrlStatus(String url) {
        URL connectionUrl = null;
        try {
            connectionUrl = getURL(url);
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
            LOG.warn(e.getMessage());
            return 0;
        }
        if(connectionUrl != null && !ignoredUrls.contains(url.toString())) {
            HttpURLConnection huc = null;
            try {
                huc = (HttpURLConnection) connectionUrl.openConnection();
                huc.setRequestMethod("HEAD");
                return huc.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                LOG.warn(e.getMessage());
                return 0;
            }
        }
        return 0;
    }

    /**
     * Return valid url to get content from this url
     * @param url
     * @return
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    private URL getURL(String url) throws URISyntaxException, MalformedURLException {
        URI uri = new URI(protocols.stream().anyMatch(url::contains) ? url :
                String.format("http://%s", url));
        return uri.toURL();
    }
}

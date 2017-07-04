package org.avlasov.razborpoletov.reader.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 20/05/15.
 */
public class UrlUtils {
    private static final Logger LOG = LoggerFactory.getLogger(UrlUtils.class);

    /**
     * Search for the github link on the non github urls.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String findGithubLink(String url) throws IOException, URISyntaxException {
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
     * Get description of the project form the github page.
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String getGithubDescription(@javax.validation.constraints.Pattern(regexp = "https?://github.com/" +
            ".+/.+", message = "Incorrect github url") String url) throws IOException, URISyntaxException {
        String description = null;
        URLConnection huc = getURL(url).openConnection();
        Document document = Jsoup.parse(huc.getInputStream(), null, url);
        if(Objects.nonNull(document)) {
            Elements descriptionElement = document.getElementsByClass("repository-meta-content");
            if(Objects.nonNull(descriptionElement) && descriptionElement.size() != 0) {
                Elements about = descriptionElement.first().getElementsByAttributeValue("itemprop", "about");
                if (about.size() != 0) {
                    description = about.stream().findFirst().get().textNodes().stream().findFirst().get().getWholeText()
                            .trim();
                } else {
                    LOG.warn("Description for the github link {} is not found", url);
                }
            } else {
                LOG.warn("Description element for link {} is not found", url);
            }

        } else {
            LOG.warn("Could parse Document object from github link {}", url);
        }
        return description;
    }

    /**
     * Check status of the url.
     * @param url
     * @return Responce code of the url
     * @throws URISyntaxException
     * @throws IOException
     */
    public static int checkUrlStatus(String url) {
        URL connectionUrl;
        try {
            connectionUrl = getURL(url);
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
            LOG.warn(e.getMessage());
            return 0;
        }
        if(!Constants.IGNORED_URLS.contains(url)) {
            HttpURLConnection huc;
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
     * @return URL in correct format for execution
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static URL getURL(String url) throws URISyntaxException, MalformedURLException {
        URI uri = new URI(Constants.PROTOCOLS.stream().anyMatch(url::contains) ? url :
                String.format("http://%s", url));
        return uri.toURL();
    }
}

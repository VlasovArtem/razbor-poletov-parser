package org.avlasov.razborpoletov.reader.parser.data;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.twitter.TwitterAPI;
import org.avlasov.razborpoletov.reader.utils.AsciidocUtils;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.avlasov.razborpoletov.reader.utils.MarkdownUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 25/06/2017.
 */
@Component
public class UserParser implements Parser<User> {

    private final static List<String> LIST_OF_EXCLUDED_TWITTER_ACCOUNTS = Collections.singletonList("JavaOneRussia");
    private final static Logger LOGGER = LogManager.getLogger(UserParser.class);
    private TwitterAPI twitterAPI;

    /**
     * Instantiates a new User parser.
     *
     * @param twitterAPI the twitter api
     */
    @Autowired
    public UserParser(TwitterAPI twitterAPI) {
        this.twitterAPI = twitterAPI;
    }

    /**
     * Parse information from twitter account. Name, bio, img, location, amount of appearance in episodes.
     *
     * @param files List of files that associated with each podcast episode
     * @return map that contains key guest object, that contains useful information and value that matches total
     * appearance in podcast (one per episode).
     */
    @Override
    public List<User> parse(List<File> files) {
        Map<String, Set<Integer>> twitterInformation = new HashMap<>();
        files.forEach(file -> parsePodcastTwitterInformation(file, twitterInformation));
        return twitterInformation.entrySet()
                .stream()
                .map(collectUserInformation())
                .filter(Objects::nonNull)
                .unordered()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<User> parse(File file) {
        return parse(Collections.singletonList(file));
    }

    private Function<Map.Entry<String, Set<Integer>>, User> collectUserInformation() {
        return twitterNameEpisodeEntrySet -> {
            LOGGER.debug("Twitter information will be get by the twitter name {} from Twitter API.", twitterNameEpisodeEntrySet.getKey());
            return twitterAPI.getTwitterUser(twitterNameEpisodeEntrySet.getKey())
                    .map(twitterUser -> User.builder()
                            .bio(twitterUser.getDescription())
                            .location(twitterUser.getLocation())
                            .name(twitterUser.getName())
                            .twitterAccount(twitterUser.getScreenName())
                            .twitterAccountUrl("https://twitter.com/" + twitterUser.getScreenName())
                            .twitterId(twitterUser.getId())
                            .episodes(twitterNameEpisodeEntrySet.getValue())
                            .twitterImgUrl(getOriginalTwitterUrlLink(twitterUser.getId(), twitterUser.getProfileImageUrl()))
                            .build())
                    .orElse(null);
        };
    }

    private String getOriginalTwitterUrlLink(long twitterid, String profileImageUrl) {
        String imageURL = profileImageUrl.replace("_normal", "_400x400");
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject(imageURL, String.class);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            LOGGER.warn("Squared image is not found for twitter account {}. We will try to find original image.", twitterid);
            imageURL = profileImageUrl.replace("_normal", "");
            try {
                restTemplate.getForObject(imageURL, String.class);
            } catch (Exception ex) {
                LOGGER.warn(ex.getMessage());
                LOGGER.warn("Original image is not found for twitter with id {}. Image will be empty.", twitterid);
            }
        }
        return imageURL;
    }

    private void parsePodcastTwitterInformation(File file, Map<String, Set<Integer>> twitterInformation) {
        Optional<Integer> podcastId = PodcastFileUtils.getPodcastNumber(file);
        if (podcastId.isPresent()) {
            LOGGER.debug("Start collecting users twitter information from podcast number {}.", podcastId.get());
            try {
                List<String> twitterNames = getHtmlDocument(file)
                        .stream()
                        .map(mapTwitterLink())
                        .map(getTwitterNameFromUrl())
                        .map(String::toLowerCase)
                        .distinct()
                        .collect(Collectors.toList());
                if (podcastId.get() == 0) {
                    twitterNames.add("gamussa");
                }
                if (podcastId.get() == 145) {
                    twitterNames.add("tolkv");
                    twitterNames.add("golodnyj");
                }
                LOGGER.debug("Next links was collected from podcast {}: \n{}", podcastId.get(), twitterNames.stream().collect(Collectors.joining("\n")));
                twitterNames.forEach(twitterLink -> twitterInformation.compute(twitterLink, (tn, episodes) -> {
                    if (Objects.isNull(episodes)) {
                        episodes = new HashSet<>();
                    }
                    episodes.add(podcastId.get());
                    return episodes;
                }));
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }
    }

    private Function<String, String> getTwitterNameFromUrl() {
        return twitterUrl -> {
            String[] twitterData = twitterUrl.split("/");
            if (twitterData.length > 1) {
                return twitterData[twitterData.length - 1];
            }
            return null;
        };
    }

    private List<Element> getHtmlDocument(File file) throws IOException {
        switch (FilenameUtils.getExtension(file.getAbsolutePath())) {
            case Constants.ASCII_DOC:
                List<Document> documents = AsciidocUtils.parseTwitterPart(file);
                return documents.stream()
                        .flatMap(document -> findTwitterElements(document).stream())
                        .collect(Collectors.toList());
            case Constants.MARKDOWN_FORMAT:
                return findTwitterElements(Jsoup.parse(MarkdownUtils.parseToHtml(file)));
            case Constants.HTML:
                return findTwitterElements(Jsoup.parse(file, "UTF-8"));
        }
        return Collections.emptyList();
    }

    private List<Element> findTwitterElements(Document document) {
        if (Objects.nonNull(document)) {
            Elements elements = document.getElementsByTag("a");
            return elements.stream()
                    .filter(element -> {
                        String href = element.attr("href");
                        if (href.contains("twitter.com")) {
                            String twitterLink = mapTwitterLink().apply(element);
                            return LIST_OF_EXCLUDED_TWITTER_ACCOUNTS.stream().noneMatch(href::contains) && twitterLink.matches("http(s)?://.*twitter.com/\\w+$");
                        }
                        return false;
                    })
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Function<Element, String> mapTwitterLink() {
        return element -> {
            String accountUrl = element.attributes().get("href");
            if (accountUrl != null && accountUrl.contains("/#!")) {
                String[] splitedAccountUrl = accountUrl.split("/#!");
                accountUrl = splitedAccountUrl[0] + splitedAccountUrl[1];
            }
            if (accountUrl != null && accountUrl.contains("/@")) {
                String[] splitedAccountUrl = accountUrl.split("/@");
                accountUrl = splitedAccountUrl[0] + "/" + splitedAccountUrl[1];
            }
            if (accountUrl != null && !accountUrl.contains("https")) {
                accountUrl = accountUrl.replace("http", "https");
            }
            return accountUrl;
        };
    }

}

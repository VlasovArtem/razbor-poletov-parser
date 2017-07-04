package org.avlasov.razborpoletov.reader.parser.statistic;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.entity.statistic.UserStatistic;
import org.avlasov.razborpoletov.reader.utils.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 25/06/2017.
 */
@Component
public class UserDataParser {

    private final static List<String> LIST_OF_EXCLUDED_TWITTER_ACCOUNTS = Arrays.asList("JavaOneRussia");
    private final static Logger LOGGER = LogManager.getLogger(UserDataParser.class);
    private final List<User> users;

    public UserDataParser() {
        users = new ArrayList<>();
    }

    public UserStatistic getUserStatistic(List<File> files) {
        return new UserStatistic(parserTwitterAccountInformation(files));
    }

    /**
     * Parse information from twitter account. Name, bio, img, location, amount of appearance in episodes.
     *
     * @param files List of files that associated with each podcast episode
     * @return map that contains key guest object, that contains useful information and value that matches total
     * appearance in podcast (one per episode).
     */
    private List<User> parserTwitterAccountInformation(List<File> files) {
        try {
            for (File file : files) {
                Optional<Integer> podcastId = PodcastFileUtils.getPodcastId(file);
                if (podcastId.isPresent()) {
                    LOGGER.info("Start parsing {}", file.getName());
                    List<User> users = parseTwitter(getHtmlDocument(file), podcastId.get());
                    if (Objects.isNull(users)) {
                        LOGGER.info("File {} twitter parsing is unsuccessful", file);
                    } else {
                        LOGGER.info("New twitter accounts found in podcast {}", users.stream().map(User::getTwitterAccount).collect(Collectors.joining(", ")));
                        countTwitter(users, podcastId.get());
                    }
                }
            }
            return users.stream().unordered().sorted().collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Element> getHtmlDocument(File file) throws IOException {
        FilenameUtils.getExtension(file.getAbsolutePath());
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

    private List<User> parseTwitter(List<Element> filteredElements, int podcastId) {
        return filteredElements
                .stream()
                .map(mapTwitterLink())
                .distinct()
                .map(getGuest(podcastId))
                .filter(guest -> !User.EMPTY_USER.equals(guest))
                .collect(Collectors.toList());
    }

    private User mapGuestDataFromUrl(String accountURL, int podcastId) {
        return TwitterUtils.getInstance().buildGuestFromTwitterAccount(accountURL, podcastId);
    }

    private void countTwitter(List<User> users, int podcastId) {
        users.forEach(guest -> {
            int index = this.users.indexOf(guest);
            if (index == -1) {
                this.users.add(guest);
            } else {
                User userData = this.users.get(index);
                userData.addAppearance();
                userData.addEpisodeNumber(podcastId);
            }
        });
    }

    private Function<String, User> getGuest(int podcastId) {
        return accountURL ->
            users
                    .stream()
                    .filter(guestData -> guestData.isRequiredGuest(accountURL))
                    .findFirst()
                    .orElseGet(() -> mapGuestDataFromUrl(accountURL, podcastId));
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

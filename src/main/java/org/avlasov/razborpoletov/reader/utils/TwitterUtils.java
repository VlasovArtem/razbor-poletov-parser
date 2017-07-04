package org.avlasov.razborpoletov.reader.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by artemvlasov on 25/06/2017.
 */
public class TwitterUtils {

    private final static Logger LOGGER = LogManager.getLogger(TwitterUtils.class);

    private static class TwitterUtilsHelper {
        private static final TwitterUtils INSTANCE = new TwitterUtils();
    }

    public static TwitterUtils getInstance() {
        return TwitterUtilsHelper.INSTANCE;
    }

    public User buildGuestFromTwitterAccount(String twitterAccountURL, int podcastId) {
        try {
            return new TwitterGuestBuilder(twitterAccountURL, podcastId).build();
        } catch (IOException e) {
            LOGGER.warn("Twitter URL {} is not valid.", twitterAccountURL);
            return User.EMPTY_USER;
        }
    }

    private static class TwitterGuestBuilder {
        private String twitterURL;
        private Document document;
        private int podcastId;

        public TwitterGuestBuilder(String twitterURL, int podcastId) throws IOException {
            this.twitterURL = twitterURL;
            this.document = Jsoup.connect(twitterURL).get();
            this.podcastId = podcastId;
        }

        private String getName() {
            return getElementByCSS(".ProfileHeaderCard-nameLink");
        }

        private String getTwitterImgURL() {
            Elements elementsByClass = document.getElementsByClass("ProfileAvatar-image");
            if (Objects.nonNull(elementsByClass) && !elementsByClass.isEmpty()) {
                return elementsByClass.attr("src");
            }
            return "";
        }

        private String getTwitter() {
            return getElementByCSS(".ProfileHeaderCard-screennameLink");
        }

        private String getBio() {
            return getElementByCSS(".ProfileHeaderCard-bio");
        }

        private String getLocation() {
            return getElementByCSS(".ProfileHeaderCard-location > .ProfileHeaderCard-locationText");
        }

        private String getElementByCSS(String css) {
            Elements elementsByClass = document.select(css);
            if (Objects.nonNull(elementsByClass) && !elementsByClass.isEmpty()) {
                return elementsByClass.first().text();
            }
            return "";
        }

        public User build() {
            return new User(getTwitter(), twitterURL,getName(), getTwitterImgURL(),getLocation(),getBio(), podcastId);
        }

    }

}

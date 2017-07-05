package org.avlasov.razborpoletov.reader.matchers;

import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Created by artemvlasov on 05/07/2017.
 */
public class PodcastLinkMatcher extends BaseMatcher<PodcastLink> {

        private PodcastLink podcastLink;

        public PodcastLinkMatcher(PodcastLink podcastLink) {
            this.podcastLink = podcastLink;
        }

        @Override
        public boolean matches(Object item) {
            PodcastLink expectedPodcast = (PodcastLink) item;
            return podcastLink.getLink().equals(expectedPodcast.getLink()) && podcastLink.getPodcastNumber() == expectedPodcast.getPodcastNumber();
        }

        @Override
        public void describeTo(Description description) {

        }
}

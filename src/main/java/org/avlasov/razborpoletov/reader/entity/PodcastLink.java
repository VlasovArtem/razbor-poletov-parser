package org.avlasov.razborpoletov.reader.entity;

import java.util.Objects;

/**
 * Created by artemvlasov on 04/07/2017.
 */
public class PodcastLink implements Comparable<PodcastLink> {

    private int podcastNumber;
    private String link;

    public PodcastLink(int podcastNumber, String link) {
        this.podcastNumber = podcastNumber;
        this.link = link;
    }

    public int getPodcastNumber() {
        return podcastNumber;
    }

    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PodcastLink)) return false;

        PodcastLink that = (PodcastLink) o;

        return podcastNumber == that.podcastNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(podcastNumber);
    }

    @Override
    public int compareTo(PodcastLink o) {
        return Integer.compare(this.podcastNumber, o.podcastNumber);
    }
}

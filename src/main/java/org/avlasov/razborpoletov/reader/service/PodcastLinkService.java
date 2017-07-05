package org.avlasov.razborpoletov.reader.service;

import org.avlasov.razborpoletov.reader.entity.PodcastLink;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Created by artemvlasov on 04/07/2017.
 */
public interface PodcastLinkService {

    List<PodcastLink> parseAllPodcastLinks();
    List<PodcastLink> parsePodcastLinks(int... podcastNumbers);
    List<PodcastLink> parsePodcastLinks(List<File> files);
    Optional<PodcastLink> parsePodcastLink(File file);
    Optional<PodcastLink> parsePodcastLink(int podcastNumber);
    Optional<File> savePodcastLinksToJson(List<PodcastLink> podcastLinks, boolean append);
    Optional<File> savePodcastLinksToJson(PodcastLink podcastLink, boolean append);

    List<PodcastLink> prepareLinksForSave(List<PodcastLink> newPodcastLinks, boolean updateRequired);
}

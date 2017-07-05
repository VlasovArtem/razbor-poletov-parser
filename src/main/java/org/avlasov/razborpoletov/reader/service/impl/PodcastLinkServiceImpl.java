package org.avlasov.razborpoletov.reader.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.exception.PodcastLinksServiceException;
import org.avlasov.razborpoletov.reader.parser.data.LinkParser;
import org.avlasov.razborpoletov.reader.service.PodcastLinkService;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by artemvlasov on 04/07/2017.
 */
@Service
public class PodcastLinkServiceImpl implements PodcastLinkService {

    private static final Logger LOGGER = LogManager.getLogger(PodcastLinkServiceImpl.class);
    private final LinkParser linkParser;
    private final ObjectMapper objectMapper;
    private final String podcastLinksJsonName = "podcast-links.json";

    @Autowired
    public PodcastLinkServiceImpl(LinkParser linkParser, ObjectMapper objectMapper) {
        this.linkParser = linkParser;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PodcastLink> parseAllPodcastLinks() {
        return linkParser.parseAllPodcastLinks();
    }

    @Override
    public List<PodcastLink> parsePodcastLinks(int... podcastNumbers) {
        return linkParser.parsePodcastLinks(podcastNumbers);
    }

    @Override
    public List<PodcastLink> parsePodcastLinks(List<File> files) {
        if (Objects.nonNull(files) && !files.isEmpty()) {
            return linkParser.parsePodcastLinks(PodcastFileUtils.getPodcastsIdArray(files));
        }
        return Collections.emptyList();
    }

    /**
     * Parse podcast link from the file, podcast number will be parsed from file
     *
     * @param file podcast
     * @return
     */
    @Override
    public Optional<PodcastLink> parsePodcastLink(File file) {
        if (Objects.nonNull(file)) {
            Optional<Integer> podcastId = PodcastFileUtils.getPodcastId(file);
            if (podcastId.isPresent()) {
                return linkParser.parsePodcastLink(podcastId.get());
            }
        }
        return Optional.empty();
    }

    /**
     * Parse podcast link by podcast number
     *
     * @param podcastNumber podcast number
     * @return Optional
     */
    @Override
    public Optional<PodcastLink> parsePodcastLink(int podcastNumber) {
        return linkParser.parsePodcastLink(podcastNumber);
    }

    /**
     * Save podcast links to the file. If {@param append} is true, then system will try to collect data from file (if file is exists) and update existing file data with new content.
     * Existing content if its matches by podcast number will be override. Otherwise new data will be added to the file.
     *
     * @param podcastLinks List of podcast links
     * @param append append {@param podcastLinks} to the file or not
     * @return file if saving data was successfully completed.
     */
    @Override
    public Optional<File> savePodcastLinksToJson(List<PodcastLink> podcastLinks, boolean append) {
        if (Objects.nonNull(podcastLinks) && !podcastLinks.isEmpty()) {
            try {
                File file = new File(podcastLinksJsonName);
                objectMapper.writeValue(file, prepareLinksForSave(podcastLinks, append));
                return Optional.of(file);
            } catch (IOException e) {
                throw new PodcastLinksServiceException(e);
            }
        }
        LOGGER.warn("Podcast links are null or empty");
        return Optional.empty();
    }

    /**
     * Save single podcast link to the file
     *
     * @param podcastLink Podcast Link
     * @param append append data to the existing file
     * @return file if saving data was successfully completed.
     */
    @Override
    public Optional<File> savePodcastLinksToJson(PodcastLink podcastLink, boolean append) {
        return savePodcastLinksToJson(Collections.singletonList(podcastLink), append);
    }

    @Override
    public List<PodcastLink> prepareLinksForSave(List<PodcastLink> newPodcastLinks, boolean updateRequired) {
        try {
            if (updateRequired) {
                List<PodcastLink> existingLinks = objectMapper.readValue(new File(podcastLinksJsonName), objectMapper.getTypeFactory().constructCollectionType(List.class, PodcastLink.class));
                return Stream.concat(existingLinks
                        .stream()
                        .filter(podcastLink ->
                                !newPodcastLinks.contains(podcastLink)), newPodcastLinks.stream())
                        .collect(Collectors.toList());
            }
            return newPodcastLinks;
        } catch (IOException e) {
            throw new PodcastLinksServiceException(e);
        }
    }
}

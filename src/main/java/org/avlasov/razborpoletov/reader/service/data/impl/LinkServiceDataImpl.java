package org.avlasov.razborpoletov.reader.service.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.avlasov.razborpoletov.reader.cli.ParserCommandLine;
import org.avlasov.razborpoletov.reader.entity.PodcastLink;
import org.avlasov.razborpoletov.reader.parser.data.LinkParser;
import org.avlasov.razborpoletov.reader.service.data.ExtendableServiceDataAbstract;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created By artemvlasov on 07/01/2018
 **/
@Service
public class LinkServiceDataImpl extends ExtendableServiceDataAbstract<PodcastLink, LinkParser> {

    @Autowired
    public LinkServiceDataImpl(LinkParser parser, ObjectMapper objectMapper, ParserCommandLine parserCommandLine, PodcastFolderUtils folderUtils) {
        super(parser, objectMapper, parserCommandLine, folderUtils);
    }

    @Override
    protected Class<PodcastLink> getDataClass() {
        return PodcastLink.class;
    }

    @Override
    protected List<PodcastLink> mergeUsersData(List<PodcastLink> jsonCollectedData, List<PodcastLink> newParsedDataFromPodcastsFiles) {
        jsonCollectedData
                .removeIf(newParsedDataFromPodcastsFiles::contains);
        List<PodcastLink> podcastLinks = new ArrayList<>(jsonCollectedData);
        podcastLinks.addAll(newParsedDataFromPodcastsFiles);
        return podcastLinks
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    protected int getLastParsedPodcastNumber() {
        return getJsonData()
                .stream()
                .mapToInt(PodcastLink::getPodcastNumber)
                .max()
                .orElse(-999);
    }

    @Override
    protected String getFileName() {
        return "links";
    }

}

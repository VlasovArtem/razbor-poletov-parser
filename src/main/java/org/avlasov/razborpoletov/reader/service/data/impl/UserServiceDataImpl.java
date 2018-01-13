package org.avlasov.razborpoletov.reader.service.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.cli.ParserCommandLine;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.enums.CreatorsGuestsArgument;
import org.avlasov.razborpoletov.reader.parser.data.UserParser;
import org.avlasov.razborpoletov.reader.service.data.ExtendableServiceDataAbstract;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created By artemvlasov on 05/01/2018
 */
@Service
public class UserServiceDataImpl extends ExtendableServiceDataAbstract<User, UserParser> {

    private static final Logger LOGGER = LogManager.getLogger(UserServiceDataImpl.class);

    @Autowired
    public UserServiceDataImpl(UserParser parser, ObjectMapper objectMapper, ParserCommandLine parserCommandLine, PodcastFolderUtils folderUtils) {
        super(parser, objectMapper, parserCommandLine, folderUtils);
    }

    @Override
    protected Class<User> getDataClass() {
        return User.class;
    }

    @Override
    protected String getFileName() {
        return "users";
    }

    @Override
    protected int getLastParsedPodcastNumber() {
        return getJsonData()
                .stream()
                .flatMap(user -> user.getAppearanceEpisodeNumbers().stream())
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-999);
    }

    /**
     * Check if all files podcasts parsing required.
     *
     * @return {@link true} if {@link CreatorsGuestsArgument#ALL} (see {@link UserServiceDataImpl#getCreatorsGuestsArgument()} or {@link UserServiceDataImpl#jsonData} return empty collection.
     */
    @Override
    protected boolean isAllFilesParsingRequired() {
        return CreatorsGuestsArgument.ALL.equals(getCreatorsGuestsArgument()) || getJsonData().isEmpty();
    }

    /**
     * Get Creators ang Guests Argument {@link CreatorsGuestsArgument}
     *
     * @return {@link CreatorsGuestsArgument#ALL} if command line argument contains all text, otherwise {@link false}
     */
    private CreatorsGuestsArgument getCreatorsGuestsArgument() {
        return parserCommandLine.getOption(CommandLineArgument.CREATORS_GUESTS)
                .map(option -> {
                    try {
                        return CreatorsGuestsArgument.valueOf(option.getValue().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return CreatorsGuestsArgument.UPDATE;
                    }
                })
                .orElse(CreatorsGuestsArgument.UPDATE);
    }

    @Override
    protected List<User> mergeUsersData(List<User> jsonCollectedData, List<User> newParsedDataFromPodcastsFiles) {
        newParsedDataFromPodcastsFiles
                .forEach(computeUser(jsonCollectedData));
        return jsonCollectedData;
    }

    /**
     * Compute parsed user to json collected data;
     *
     * @param jsonCollectedData Collected data
     * @return Consumer
     */
    private Consumer<User> computeUser(List<User> jsonCollectedData) {
        return parsedUsed -> {
            if (jsonCollectedData.contains(parsedUsed)) {
                int userIndex = jsonCollectedData.indexOf(parsedUsed);
                User user = jsonCollectedData.get(userIndex);
                Set<Integer> appearanceEpisodeNumbers = user.getAppearanceEpisodeNumbers();
                appearanceEpisodeNumbers.addAll(parsedUsed.getAppearanceEpisodeNumbers());
                User updatedUser = User.builder()
                        .twitterId(parsedUsed.getTwitterId())
                        .name(parsedUsed.getName())
                        .twitterAccountUrl(parsedUsed.getTwitterAccountUrl())
                        .twitterImgUrl(parsedUsed.getTwitterImgUrl())
                        .twitterAccount(parsedUsed.getTwitterAccount())
                        .location(parsedUsed.getLocation())
                        .bio(parsedUsed.getBio())
                        .episodes(appearanceEpisodeNumbers)
                        .build();
                jsonCollectedData.set(userIndex, updatedUser);
            } else {
                jsonCollectedData.add(parsedUsed);
            }
        };
    }

}

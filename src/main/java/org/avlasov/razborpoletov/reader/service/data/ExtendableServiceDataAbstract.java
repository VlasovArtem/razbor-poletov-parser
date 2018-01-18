package org.avlasov.razborpoletov.reader.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.cli.ParserCommandLine;
import org.avlasov.razborpoletov.reader.parser.data.Parser;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created By artemvlasov on 06/01/2018
 **/
public abstract class ExtendableServiceDataAbstract<T, R extends Parser<T>> extends ServiceDataAbstract<T, R> {

    private static final Logger LOGGER = LogManager.getLogger(ExtendableServiceDataAbstract.class);
    private List<T> jsonData;
    private PodcastFolderUtils folderUtils;
    protected ParserCommandLine parserCommandLine;

    public ExtendableServiceDataAbstract(R parser, ObjectMapper objectMapper, ParserCommandLine parserCommandLine, PodcastFolderUtils folderUtils) {
        super(parser, objectMapper);
        this.parserCommandLine = parserCommandLine;
        this.folderUtils = folderUtils;
        jsonData = collectJsonFileData(getDataClass());
    }

    @Override
    public List<T> parse(List<File> files) {
        if (isAllFilesParsingRequired()) {
            LOGGER.info("All data parsing required.");
            return super.parse(folderUtils.getAllPodcastFiles());
        } else if (checkIsAllUsersParsed()) {
            LOGGER.info("All data was already parsed.");
            return Collections.emptyList();
        }
        List<T> newData = super.parse(filterFilesToParse(files));
        return mergeUsersData(getJsonData(), newData);
    }

    @Override
    public File saveJsonData(List<T> data) {
        if (!data.isEmpty()) {
            return super.saveJsonData(data);
        } else {
            LOGGER.info("User data is empty. Probably all data was already save or data was not parsed from provided files.");
        }
        return new File(getFilePath("json"));
    }

    /**
     * Check if all files podcasts parsing required.
     *
     * @return {@link true} if {@link ExtendableServiceDataAbstract#jsonData} return empty collection.
     */
    protected boolean isAllFilesParsingRequired() {
        return getJsonData().isEmpty();
    }

    protected abstract Class<T> getDataClass();

    /**
     * Merge data, that was collected from json file with data, that was parsed from podcast files
     *
     * @param jsonCollectedData Data that was collected from json file {@link ExtendableServiceDataAbstract#jsonData}
     * @param newParsedDataFromPodcastsFiles Data that was parsed from podcast files
     * @return Merged collection
     */
    protected abstract List<T> mergeUsersData(List<T> jsonCollectedData, List<T> newParsedDataFromPodcastsFiles);

    /**
     * Get last parsed podcast number
     *
     * @return last podcast number
     */
    protected abstract int getLastParsedPodcastNumber();

    protected List<T> getJsonData() {
        return jsonData;
    }

    /**
     * Filter Files to parse. Method will remove podcast, that was already
     *
     * @param files files to parse
     * @return modified files to parse
     */
    private List<File> filterFilesToParse(List<File> files) {
        int[] podcastFilesToParse = files.stream()
                .mapToInt(file -> PodcastFileUtils.getPodcastNumber(file).orElse(-999))
                .sorted()
                .toArray();
        return folderUtils.getPodcastsFiles(getLastParsedPodcastNumber() + 1, podcastFilesToParse[podcastFilesToParse.length - 1]);
    }

    /**
     * Collect Data from JSON file
     *
     * @return List of data if json file is exists, otherwise return empty collection
     */
    private List<T> collectJsonFileData(Class<T> clazz) {
        File json = new File(getFilePath("json"));
        if (json.exists()) {
            try {
                return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionLikeType(List.class, clazz));
            } catch (IOException e) {
                LOGGER.error(e);
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Check if all users data was parsed from podcasta files. Method will compare last podcast id with last podcast id in parsed users.
     *
     * @return {@link true} if last podcast id and last parsed user podcast id is matches, otherwise {@link false}
     */
    private boolean checkIsAllUsersParsed() {
        return PodcastFileUtils.getPodcastNumber(folderUtils.getLastPodcastFile())
                .filter(integer -> integer == getLastParsedPodcastNumber())
                .isPresent();
    }

}

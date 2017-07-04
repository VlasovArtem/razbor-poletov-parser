package org.avlasov.razborpoletov.reader.parser.data;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 20/04/15.
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FileParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileParser.class);
    private final PodcastFolderUtils podcastFolderUtils;
    private List<File> podcastsFiles;

    @Autowired
    public FileParser(String postsFolder, PodcastFolderUtils podcastFolderUtils) throws IOException {
        this.podcastFolderUtils = podcastFolderUtils;
        podcastsFiles = this.podcastFolderUtils.collectPostsFolderPath(postsFolder);
    }

    public List<File> getPodcastsFiles() {
        return podcastsFiles;
    }

    /**
     * Find last podcast file throw Runtime Exception if the file of the last podcast is not a asciidoc file (check
     * extension of the file)
     *
     * @return Last podcast file
     */
    public File getLastPodcastFile() {
        File file = podcastsFiles.get(podcastsFiles.size() - 1);
        String asciidocFilePattern = ".+(?=(a*(sc)?i*(doc)?d?))\\.a*(sc)?i*(doc)?d?";
        if (Pattern.matches(asciidocFilePattern, file.getName())) {
            return file;
        } else {
            LOGGER.warn("Last podcast file is not asciidoc format, file format: {}", FilenameUtils.getExtension(file
                    .getName()));
            throw new RuntimeException();
        }
    }

    /**
     * Find podcast by number of the episode.
     *
     * @param number number of the episode
     * @return find file
     */
    public File getPodcastByNumber(int number) {
        RegexFileFilter filter = new RegexFileFilter(Pattern.compile(".+-" + number + "\\..+"));
        List<File> filteredFiles = FileFilterUtils.filterList(filter, podcastsFiles);
        if (!filteredFiles.isEmpty()) {
            return filteredFiles.get(0);
        }
        return null;
    }

}

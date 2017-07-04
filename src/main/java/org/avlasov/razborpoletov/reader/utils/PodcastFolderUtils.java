package org.avlasov.razborpoletov.reader.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 28/12/15.
 */
@Component
public class PodcastFolderUtils {
    public static final String PODCAST_POST_FOLDER_APPENDER;
    private static String podcastFolderPattern;
    private static String podcastPostFilePattern = "20([0-9]{2}-){3}episode(-)?[0-9].+";

    static {
        PODCAST_POST_FOLDER_APPENDER = "source" + File.separator + "_posts" + File.separator;
        podcastFolderPattern = ".+" + PODCAST_POST_FOLDER_APPENDER;
    }

    /**
     * Return podcast post folder
     * @param podcastFolderPath Podcast folder path provided by user of the API
     * @return string
     */
    public String getPodcastPostsFolder (String podcastFolderPath) {
        return StringUtils.endsWith(podcastFolderPath, "/") ?
                podcastFolderPath + PODCAST_POST_FOLDER_APPENDER :
                podcastFolderPath + "/" + PODCAST_POST_FOLDER_APPENDER;
    }

    /**
     * Check Podcast folder and Podcast post folder is exist and contains information
     * @param podcastFolderPath Podcast folder path provided by user of the API
     */
    public boolean checkPodcastFolder (String podcastFolderPath) {
        Objects.requireNonNull(podcastFolderPath, "Podcast folder path cannot be null");
        File podcastFolder = new File(podcastFolderPath);
        if(!podcastFolder.exists()) {
            throw new RuntimeException("Folder with provided path is not exists");
        } else if (podcastFolder.isDirectory()) {
            String postsFolderPath = getPodcastPostsFolder(podcastFolderPath);
            if(!checkPodcastPostsFolder(postsFolderPath)) {
                throw new RuntimeException("Posts folder does not exists or posts folder is empty");
            }
        }
        return true;
    }

    /**
     * Check folder that contains podcast posts (episodes) folder. Folder should exists and contains list of files.
     * @param folderPath path of the folder
     * @return return true if folderPath content and folder them self contain correct information, otherwise return
     * false.
     */
    public boolean checkPodcastPostsFolder (String folderPath) {
        if(Objects.isNull(folderPath)) {
            return false;
        }
        if(!folderPath.matches(podcastFolderPattern)) {
            File podcastPostsFolder = new File(folderPath);
            if(!podcastPostsFolder.exists()
                    || (podcastPostsFolder.isDirectory()
                    && Objects.nonNull(podcastPostsFolder.listFiles())
                    && collectPostsFolderPath(folderPath).size() == 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Collect list of files from posts folder, that matches by provided file pattern.
     * @param podcastFolderPath Podcast folder path, this path could match folder of the posts or path of the base folder
     *                   podcast path.
     * @return list of the file
     */
    public List<File> collectPostsFolderPath (String podcastFolderPath) {
        if(checkPodcastFolder(podcastFolderPath)) {
            File postsFolder;
            if(podcastFolderPath.matches(podcastFolderPattern)) {
                postsFolder = new File(podcastFolderPath);
            } else {
                postsFolder = new File(getPodcastPostsFolder(podcastFolderPath));
            }
            File[] postsFiles = postsFolder.listFiles();
            if(Objects.nonNull(postsFiles))
                return Arrays.stream(postsFiles)
                        .filter(file -> Pattern.matches(podcastPostFilePattern, file.getName()))
                        .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

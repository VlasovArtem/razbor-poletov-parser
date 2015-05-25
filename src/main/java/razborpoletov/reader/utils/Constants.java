package razborpoletov.reader.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by artemvlasov on 22/05/15.
 */
public class Constants {
    public final static String MARKDOWN_FORMAT = "markdown";
    public final static String ASCII_DOC = "adoc";
    public final static String HTML = "html";
    public final static String CONFERENCES_FILE = "conferences.json";
    public final static String USEFUL_THINGS_FILE = "useful-things.json";
    public final static String PROJECT_STATISTICS_FILE = "project-statistics.json";
    public final static String CONF_COMPLETE = "complete-conferences.json";
    public final static String USEFUL_THINGS_COMPLETE = "complete-useful-things.json";
    public final static String[] PROP_NAMES = {"podcasts.folder", "local.git.folder"};
    public final static String LAST_PODCAST_PROP_NAME = "last.podcast";
    public final static String PODCASTS_COUNT_PROP_NAME = "podcasts.count";
    public final static String PODCASTS_FOLDER_PROP_NAME = "podcasts.folder";
    public final static String LOCAL_GIT_FOLDER_PROP_NAME = "local.git.folder";
    public final static List<String> PROTOCOLS = Arrays.asList("http://", "https://");
    public final static List<String> IGNORED_URLS = Arrays.asList("http://www.latencytop.org/", "http://tobacco" +
            ".noroutine.me", "http://www.fossil-scm.org/index.html/doc/tip/www/quickstart.wiki");
}

package razborpoletov.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import razborpoletov.reader.parcers.FileParser;
import razborpoletov.reader.git.GitPuller;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Hello world!
 *
 */
class App {
    private static final String[] PROP_KEYS = {"podcasts.folder", "local.git.folder"};
    private static final String PODCASTS_COUNT_PROP_NAME = "podcasts.count";
    private static final String PODCASTS_FOLDER_PROP_NAME = "podcasts.folder";
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws IOException, URISyntaxException {
        String propertiesFolderPath = null;
        boolean test = false;
        PropertiesSelector propertiesSelector = null;
        if(args.length == 0) {
            throw new RuntimeException("You need to setup properties file path");
        }
        for(String arg : args) {
            if(arg.contains("properties")) {
                propertiesFolderPath = arg;
            } else if (arg.equals("test")){
                test = true;
            }
        }
        if(propertiesFolderPath == null) {
            throw new RuntimeException("You need to setup properties file path");
        }
        try {
            propertiesSelector = new PropertiesSelector(propertiesFolderPath);
            for(String prop : PROP_KEYS) {
                if(propertiesSelector.getProperty(prop) == null) {
                    throw new RuntimeException("There is incorrect data in properties file. Preferable keys: " +
                            "podcasts.count, podcasts.folder, local.git.folder");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!test) {
            try {
                GitPuller gitPuller = new GitPuller(propertiesSelector.getProperty(GitPuller.LOCAL_GIT_FOLDER_PROP_NAME));
                PullResult pullResult = gitPuller.pull();
                if(!pullResult.isSuccessful()) {
                    throw new RuntimeException("Git pull execute unsuccessfully");
                } else if(pullResult.toString().contains("Already up-to-date.")) {
                    LOG.info("All podcasts are parsed");
                }
                File file = new File(propertiesSelector.getProperty(PODCASTS_FOLDER_PROP_NAME));
                if(propertiesSelector.getProperty(PODCASTS_COUNT_PROP_NAME) == null) {
                    propertiesSelector.setProperty(PODCASTS_COUNT_PROP_NAME, 0);
                }
                if(file.listFiles() != null) {
                    if (file.listFiles().length != Integer.valueOf(propertiesSelector.getProperty(PODCASTS_COUNT_PROP_NAME))) {
                        FileParser fileParser = new FileParser(propertiesSelector.getProperty
                                (PODCASTS_FOLDER_PROP_NAME), propertiesSelector, test);
                        fileParser.fileParserLastPodcast();
                    }
                }
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            FileParser fileParser = new FileParser(propertiesSelector.getProperty(PODCASTS_FOLDER_PROP_NAME), propertiesSelector, test);
            fileParser.fileParserTwitter();
            fileParser.fileParserPodcast();
        }


    }
}

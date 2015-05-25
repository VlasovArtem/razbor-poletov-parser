package razborpoletov.reader.parcers;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import razborpoletov.reader.entity.ProjectStatistics;
import razborpoletov.reader.entity.Twitter;
import razborpoletov.reader.utils.AsciidocUtils;
import razborpoletov.reader.utils.MarkdownUtils;
import razborpoletov.reader.utils.UrlUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static razborpoletov.reader.utils.Constants.*;

/**
 * Created by artemvlasov on 20/05/15.
 */
public class StatisticParser {
    private ProjectStatistics projectStatistics;
    private Map<Twitter, Integer> twitterCount;
    private final static Logger LOG = LoggerFactory.getLogger(StatisticParser.class);

    public StatisticParser() {
        projectStatistics = new ProjectStatistics();
    }

    public ProjectStatistics parseProjectStatistics(List<File> files) throws IOException, URISyntaxException {
        findTotalGuests(files);
        findPodcastData(files);
        return projectStatistics;
    }
    public Map<Twitter, Integer> fileParserTwitter(List<File> files) throws IOException {
        for (File file : files) {
            countTwitter(parseTwitter(getHtmlDocument(file)));
        }
        return twitterCount;
    }
    private void findTotalGuests(List<File> files) throws IOException {
        Map<Twitter, Integer> count = fileParserTwitter(files);
        projectStatistics.setTotalGuests(count.size());
    }
    private void findPodcastData(List<File> files) throws IOException, URISyntaxException {
        long totalTime = 0;
        long longest = 0;
        for(File file : files) {
            long time = getPocastMp3FileLengthInMillis(getHtmlDocument(file), file);
            totalTime += time;
            longest = longest > time ? longest : time;
        }
        projectStatistics.setLongestPodcast(longest);
        projectStatistics.setTotalPodcastsTime(totalTime);
    }
    private long getPocastMp3FileLengthInMillis(Document document, File file) throws IOException, URISyntaxException {
        Elements elements = document.getElementsByTag("a");
        String url = elements.stream().filter(element -> element.attributes().get("href").contains("libsyn"))
                .findFirst()
                .get()
                .attributes().get("href");
        if(url != null) {
            URLConnection huc = UrlUtils.getURL(url).openConnection();
            InputStream in = huc.getInputStream();
            String filename = "/tmp/" + file.getName() + ".mp3";
            File mp3File = new File(filename);
            OutputStream outstream = new FileOutputStream(mp3File);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                outstream.write(buffer, 0, len);
            }
            outstream.close();
            Mp3File song = null;
            try {
                song = new Mp3File(filename);
            } catch (UnsupportedTagException | InvalidDataException e) {
                LOG.warn(e.getMessage());
                e.printStackTrace();
            }
            if (song != null) {
                file.delete();
                return song.getLengthInMilliseconds();
            }
        } else {
            LOG.warn("Podcast {} has no mp3 file link", file.getName());
        }
        return 0;
    }
    private Document getHtmlDocument(File file) throws IOException {
        Document document = null;
        FilenameUtils.getExtension(file.getAbsolutePath());
        switch (FilenameUtils.getExtension(file.getAbsolutePath())) {
            case ASCII_DOC:
                document = AsciidocUtils.parseTwitterPart(file);
                break;
            case MARKDOWN_FORMAT:
                document =  Jsoup.parse(MarkdownUtils.parseToHtml(file));
                break;
            case HTML:
                document = Jsoup.parse(file, "UTF-8");
                break;
        }
        return document;
    }

    private List<Twitter> parseTwitter(Document document) {
        Elements elements = document.getElementsByTag("a");
        List<Element> filteredElements = elements.stream().filter(element -> element.attr("href").contains
                ("twitter.com")
                &&
                element.textNodes()
                        .stream().allMatch(node -> node.getWholeText().contains("@"))).collect(Collectors.toList());
        List<Twitter> twitters = new ArrayList<>(filteredElements.size());
        for(Element el : filteredElements) {
            String account = el.textNodes().stream().findFirst().get().getWholeText();
            String accountUrl = el.attributes().get("href");
            if(accountUrl != null && accountUrl.contains("/#!")) {
                String[] splitedAccountUrl = accountUrl.split("/#!");
                accountUrl = splitedAccountUrl[0] + splitedAccountUrl[1];
            }
            if(accountUrl != null && accountUrl.contains("/@")) {
                String[] splitedAccountUrl = accountUrl.split("/@");
                accountUrl = splitedAccountUrl[0] + "/" + splitedAccountUrl[1];
            }
            if(accountUrl != null && !accountUrl.contains("https")) {
                accountUrl = accountUrl.replace("http", "https");
            }
            twitters.add(new Twitter(account, accountUrl));
        }
        return twitters;
    }

    private void countTwitter(List<Twitter> twitters) {
        if(twitters != null) {
            for(Twitter twitter: twitters) {
                if (!twitterCount.containsKey(twitter)) {
                    twitterCount.put(twitter, 1);
                } else {
                    twitterCount.put(twitter, twitterCount.get(twitter) + 1);
                }
            }
        }
    }
}

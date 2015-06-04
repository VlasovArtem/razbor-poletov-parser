package razborpoletov.reader.parsers;

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
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            long time = getPocastMp3FileLengthInMillis(file);
            totalTime += time;
            longest = longest > time ? longest : time;
        }
        projectStatistics.setLongestPodcast(longest);
        projectStatistics.setTotalPodcastsTime(totalTime);
    }
    public File getFile(File file) throws IOException, URISyntaxException {
        String url = getUrl(file);
        if(url != null) {
            URLConnection huc = UrlUtils.getURL(url).openConnection();
            InputStream in = huc.getInputStream();
            String filename = "/tmp/" + file.getName() + ".mp3";
            return new File(filename);
        }
        return null;
    }
    public long getPocastMp3FileLengthInMillis(File file) throws IOException, URISyntaxException {
        Mp3File mp3 = getAudioFile(file);
        if(mp3 != null) {
            long millis = mp3.getLengthInMilliseconds();
            getFile(file).delete();
            return millis;
        }
        return 0;
    }
    public Document getHtmlDocument(File file) throws IOException {
        Document document = null;
        FilenameUtils.getExtension(file.getAbsolutePath());
        switch (FilenameUtils.getExtension(file.getAbsolutePath())) {
            case ASCII_DOC:
                document = AsciidocUtils.parseTwitterPart(file); //???
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
    public String getMp3Filename(File file) throws IOException, URISyntaxException {
        String url = getUrl(file);
        Pattern pattern = Pattern.compile("\\w+.mp3");
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()) {
            return matcher.group().split("\\.")[0];
        }
        return null;
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
    public String getUrl(File file) throws IOException, URISyntaxException {
        Elements elements = getHtmlDocument(file).getElementsByTag("a");
        return elements.stream().filter(element -> element.attributes().get("href").contains("libsyn"))
                .findFirst()
                .get()
                .attributes().get("href").replaceAll("\\s", "");
    }
    private Mp3File getAudioFile(File file) throws IOException, URISyntaxException {
        String url = getUrl(file);
        File mp3File = getFile(file);
        if(mp3File != null) {
            URLConnection huc = UrlUtils.getURL(url).openConnection();
            InputStream in = huc.getInputStream();
            OutputStream outstream = new FileOutputStream(mp3File);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                outstream.write(buffer, 0, len);
            }
            outstream.close();
            Mp3File song = null;
            try {
                song = new Mp3File(mp3File);
            } catch (UnsupportedTagException | InvalidDataException e) {
                LOG.warn(e.getMessage());
                e.printStackTrace();
            }
            return song;
        } else {
            LOG.warn("Podcast {} has no mp3 file link", mp3File.getName());
        }
        return null;
    }
    public int getFileSize(File file) throws IOException, URISyntaxException {
        URL url = new URL(getUrl(file));
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            return -1;
        } finally {
            conn.disconnect();
        }
    }
}

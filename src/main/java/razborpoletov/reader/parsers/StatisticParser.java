package razborpoletov.reader.parsers;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.ContentPart;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static razborpoletov.reader.utils.Constants.*;

/**
 * Created by artemvlasov on 20/05/15.
 */
public class StatisticParser {
    private ProjectStatistics projectStatistics;
    private Map<Twitter, Integer> twitterCount = new HashMap<>();
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
            List<Twitter> twitters = parseTwitter(getHtmlDocument(file));
            if (Objects.isNull(twitters)) {
                LOG.info("File {} twitter parsing is unsuccessful", file);
            } else {
                countTwitter(twitters);
            }
        }
        return twitterCount;
    }

    public String getMp3Filename(String url) throws IOException, URISyntaxException {
        if (Objects.nonNull(url)) {
            Pattern pattern = Pattern.compile("\\w+.mp3");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group().split("\\.")[0];
            }
        }
        return null;
    }

    public int getFileSize(String stringUrl) throws IOException, URISyntaxException {
        if (Objects.nonNull(stringUrl)) {
            URL url = new URL(stringUrl);
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                return conn.getContentLength();
            } catch (IOException e) {
                return -1;
            } finally {
                if (Objects.nonNull(conn)) {
                    conn.disconnect();
                }
            }
        }
        return -1;
    }

    private void findTotalGuests(List<File> files) throws IOException {
        Map<Twitter, Integer> count = fileParserTwitter(files);
        FileParser.saveTwitterCountToFile(twitterCount);
        projectStatistics.setTotalGuests(count.size());
    }

    private void findPodcastData(List<File> files) throws IOException, URISyntaxException {
        long totalTime = 0;
        long longest = 0;
        for (File file : files) {
            String url = getUrl(file);
            long time = getPocastMp3FileLengthInMillis(url, file);
            totalTime += time;
            longest = longest > time ? longest : time;
            if (time == 0) {
                LOG.info("Cannot count time of the podcast from file {}", file.getName());
            }
        }
        projectStatistics.setLongestPodcastMillis(longest);
        projectStatistics.setTotalPodcastsTimeMillis(totalTime);
    }

    private File getFile(String url, File file) throws IOException, URISyntaxException {
        if (Objects.nonNull(url)) {
//            URLConnection huc = UrlUtils.getURL(url).openConnection();
//            InputStream in = huc.getInputStream();
            String filename = "/tmp/" + file.getName() + ".mp3";
            return new File(filename);
        }
        return null;
    }

    private long getPocastMp3FileLengthInMillis(String url, File file) throws IOException, URISyntaxException {
        Mp3File mp3 = getAudioFile(url, file);
        if (Objects.nonNull(mp3)) {
            long millis = mp3.getLengthInMilliseconds();
            File mp3File = getFile(url, file);
            if (mp3File != null && mp3File.exists()) {
                mp3File.delete();
            }
            return millis;
        }
        return 0;
    }

    private Document getHtmlDocument(File file) throws IOException {
        Document document = null;
        FilenameUtils.getExtension(file.getAbsolutePath());
        switch (FilenameUtils.getExtension(file.getAbsolutePath())) {
            case ASCII_DOC:
                document = AsciidocUtils.parseTwitterPart(file); //???
                break;
            case MARKDOWN_FORMAT:
                document = Jsoup.parse(MarkdownUtils.parseToHtml(file));
                break;
            case HTML:
                document = Jsoup.parse(file, "UTF-8");
                break;
        }
        return document;
    }

    private List<Twitter> parseTwitter(Document document) {
        if (Objects.nonNull(document)) {
            Elements elements = document.getElementsByTag("a");
            List<Element> filteredElements = elements.stream()
                    .filter(element -> element.attr("href").contains("twitter.com") &&
                            element.textNodes().stream()
                                    .allMatch(node -> node.getWholeText().contains("@")))
                    .collect(Collectors.toList());
            File file = new File("parsed_twitter.txt");
            if(file.exists()) {
                filteredElements.stream().forEach(element -> {
                    try {
                        FileUtils.writeLines(file, Collections.singletonList(element.attr("href")), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            List<Twitter> twitters = new ArrayList<>(filteredElements.size());
            for (Element el : filteredElements) {
                String account = el.textNodes().stream().findFirst().get().getWholeText();
                String accountUrl = el.attributes().get("href");
                if (accountUrl != null && accountUrl.contains("/#!")) {
                    String[] splitedAccountUrl = accountUrl.split("/#!");
                    accountUrl = splitedAccountUrl[0] + splitedAccountUrl[1];
                }
                if (accountUrl != null && accountUrl.contains("/@")) {
                    String[] splitedAccountUrl = accountUrl.split("/@");
                    accountUrl = splitedAccountUrl[0] + "/" + splitedAccountUrl[1];
                }
                if (accountUrl != null && !accountUrl.contains("https")) {
                    accountUrl = accountUrl.replace("http", "https");
                }
                twitters.add(new Twitter(StringUtils.endsWith(account, " ") ?
                        account.substring(0, account.length() - 1) :
                        account, accountUrl));
            }
            twitters = twitters.stream().distinct().collect(Collectors.toList());
            return twitters;
        }
        return null;
    }

    private void countTwitter(List<Twitter> twitters) {
        if (Objects.nonNull(twitters)) {
            for (Twitter twitter : twitters) {
                if (!twitterCount.containsKey(twitter)) {
                    twitterCount.put(twitter, 1);
                } else {
                    twitterCount.put(twitter, twitterCount.get(twitter) + 1);
                }
            }
        }
    }

    public String getUrl(File file) throws IOException, URISyntaxException {
        Elements elements = null;
        if (ASCII_DOC.equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
            List<ContentPart> notFilteredParts = AsciidocUtils.parseDocument(file).getParts();
            List<ContentPart> contentParts = notFilteredParts.stream()
                    .filter(d -> d.getContent().contains("libsyn") && d.getContent().contains("href"))
                    .collect(Collectors.toList());
            if (contentParts.size() > 0) {
                elements = Jsoup.parse(contentParts.get(0).getContent()).getElementsByTag("a");
            }
        } else {
            elements = getHtmlDocument(file).getElementsByTag("a");
        }
        if (Objects.nonNull(elements)) {
            List<Element> elements1 = elements.stream()
                    .filter(element -> element.attributes().get("href").contains("libsyn"))
                    .collect(Collectors.toList());
            if (elements1.size() == 0) {
                elements.stream()
                        .filter(element -> element.attributes().get("src").contains("libsyn"))
                        .collect(Collectors.toList());
            }
            if (elements1.size() > 1) {
                LOG.info("File {} contains more than one element", file.toString());
            } else if (elements1.size() == 1) {
                return elements1.get(0).attributes().get("href").replaceAll("\\s", "");
            }
            LOG.info("File {} has no audio tag", file.toString());
        }
        return null;
    }

    private Mp3File getAudioFile(String url, File file) throws IOException, URISyntaxException {
        File mp3File = getFile(url, file);
        if (Objects.nonNull(url) && Objects.nonNull(mp3File)) {
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
            LOG.warn("Podcast {} has no mp3 file link", mp3File != null ? mp3File.getName() : "");
        }
        return null;
    }
}

package org.avlasov.razborpoletov.reader.parser.statistic;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asciidoctor.ast.ContentPart;
import org.avlasov.razborpoletov.reader.entity.statistic.PodcastStatistic;
import org.avlasov.razborpoletov.reader.utils.AsciidocUtils;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.avlasov.razborpoletov.reader.utils.UrlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 25/06/2017.
 */
@Component
public class PodcastStatisticParser {

    private static final Logger LOGGER = LogManager.getLogger(PodcastStatisticParser.class);

    /**
     * Parse statistic of ea
     *
     * @param files
     * @throws IOException
     * @throws URISyntaxException
     */
    private PodcastStatistic findPodcastData(List<File> files) throws IOException, URISyntaxException {
        long totalTime = 0;
        long longest = 0;
        for (File file : files) {
            LOGGER.info("Parse {} statistic", file.getName());
            String url = getUrl(file);
            long time = getPocastMp3FileLengthInMillis(url, file);
            totalTime += time;
            longest = longest > time ? longest : time;
            if (time == 0) {
                LOGGER.info("Cannot count time of the podcast from file {}", file.getName());
            }
        }
        return new PodcastStatistic(longest, totalTime);
    }

    /**
     * Find mp3 filename from url
     *
     * @param url url of the audio file from podcast file
     * @return name of the mp3 file
     * @throws IOException
     * @throws URISyntaxException
     */
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

    /**
     * Find mp3 file length by provided url
     *
     * @param stringUrl url of the mp3 file
     * @return length of the file or return -1
     * @throws IOException
     * @throws URISyntaxException
     */
    public int getMP3FileLenght(String stringUrl) throws IOException, URISyntaxException {
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

    public String getUrl(File file) throws IOException, URISyntaxException {
        Elements elements = null;
        if (Constants.ASCII_DOC.equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
            List<ContentPart> notFilteredParts = AsciidocUtils.parseDocument(file).getParts();
            List<ContentPart> contentParts = notFilteredParts.stream()
                    .filter(d -> d.getContent().contains("libsyn") && d.getContent().contains("href"))
                    .collect(Collectors.toList());
            if (contentParts.size() > 0) {
                elements = Jsoup.parse(contentParts.get(0).getContent()).getElementsByTag("a");
            }
        } else {
//            elements = getHtmlDocument(file)
//                    .stream()
//                    .flatMap(element -> element.getElementsByTag("a").stream())
//                    .collect(Collectors.toCollection(Elements::new));
        }
        if (Objects.nonNull(elements)) {
            List<Element> elements1 = elements.stream()
                    .filter(element -> element.attributes().get("href").contains("libsyn"))
                    .collect(Collectors.toList());
            if (elements1.size() == 0) {
                elements1 = elements.stream()
                        .filter(element -> element.attributes().get("src").contains("libsyn"))
                        .collect(Collectors.toList());
            }
            if (elements1.size() > 1) {
                LOGGER.info("File {} contains more than one element", file.toString());
            } else if (elements1.size() == 1) {
                return elements1.get(0).attributes().get("href").replaceAll("\\s", "");
            }
            LOGGER.info("File {} has no audio tag", file.toString());
        }
        return "";
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
                LOGGER.warn(e.getMessage());
                e.printStackTrace();
            }
            return song;
        } else {
            LOGGER.warn("Podcast {} has no mp3 file link", mp3File != null ? mp3File.getName() : "");
        }
        return null;
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

}

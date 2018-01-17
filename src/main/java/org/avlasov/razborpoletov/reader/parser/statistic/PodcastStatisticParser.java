package org.avlasov.razborpoletov.reader.parser.statistic;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.entity.statistic.PodcastStatistic;
import org.avlasov.razborpoletov.reader.utils.AsciidocUtils;
import org.avlasov.razborpoletov.reader.utils.Constants;
import org.avlasov.razborpoletov.reader.utils.UrlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
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
     * Find mp3 filename from url
     *
     * @param url url of the audio file from podcast file
     * @return name of the mp3 file
     */
    public String getMp3Filename(String url) {
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
     */
    public int getMP3FileLength(String stringUrl) {
        if (Objects.nonNull(stringUrl)) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(stringUrl);
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

    public String getUrl(File file) {
        if (Constants.ASCII_DOC.equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
            List<Element> elements = AsciidocUtils
                    .parseDocument(file)
                    .getParts()
                    .stream()
                    .filter(contentPart -> contentPart.getContent().contains("libsyn")
                            && contentPart.getContent().contains("href"))
                    .flatMap(contentPart -> Jsoup.parse(contentPart.getContent()).getElementsByTag("a").stream())
                    .filter(element -> element.attributes().get("href").contains("libsyn")
                            && element.attributes().get("src").contains("libsyn"))
                    .collect(Collectors.toList());
            if (elements.size() > 1) {
                LOGGER.info("File {} contains more than one element", file.toString());
            } else if (!elements.isEmpty()) {
                return elements
                        .get(0)
                        .attributes()
                        .get("href")
                        .replaceAll("\\s", "");
            }
            LOGGER.info("File {} has no audio tag", file.toString());
        }
        return "";
    }

    /**
     * Parse statistic of ea
     *
     * @param files file
     */
    public PodcastStatistic findPodcastData(List<File> files) {
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

    private long getPocastMp3FileLengthInMillis(String url, File file) {
        Mp3File mp3 = getAudioFile(url, file);
        if (Objects.nonNull(mp3)) {
            long millis = mp3.getLengthInMilliseconds();
            File mp3File = new File("/tmp/" + file.getName() + ".mp3");
            if (mp3File.exists()) {
                mp3File.delete();
            }
            return millis;
        }
        return 0;
    }

    private Mp3File getAudioFile(String url, File file) {
        try {
            if (!Objects.toString(url, "").isEmpty()) {
                File mp3File = new File("/tmp/" + file.getName() + ".mp3");
                URLConnection huc = UrlUtils.getURL(url).openConnection();
                InputStream in = huc.getInputStream();
                OutputStream outstream = new FileOutputStream(mp3File);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    outstream.write(buffer, 0, len);
                }
                outstream.close();
                try {
                    return new Mp3File(mp3File);
                } catch (UnsupportedTagException | InvalidDataException e) {
                    LOGGER.warn(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                LOGGER.warn("Podcast {} has no mp3 file link", file.getName());
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(e);
            LOGGER.error("Issue is occurred during parse audio file {}.", file.getName());
        }
        return null;
    }

}

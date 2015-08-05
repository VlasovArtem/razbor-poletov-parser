package razborpoletov.reader.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 05/08/15.
 */
public class PodcastFileUtils {
    private final static Logger LOG = LoggerFactory.getLogger(PodcastFileUtils.class);

    public static LocalDate getPodcastDate(File file) {
        int year = getDataByPattern(file.getName(), Pattern.compile("201[0-9]"));
        int month = getDataByPattern(file.getName(), Pattern.compile("\b0\b|0[1-9]|1[0-2]"));
        int day = getDataByPattern(file.getName(), Pattern.compile("0[1-9]|[1-2][0-9]|3[0-1]"));
        if(year == 0 || month == 0 || day == 0) {
            LOG.info("File {} has no one of the data: year {}, month {}, day {}", file.getName(), year, month, day);
            return null;
        }
        return LocalDate.of(year, month, day);
    }
    public static long getPodcastId(File file) {
        String podcastName = file.getName();
        String[] splited = podcastName.split("-");
        long podcastId = 0;
        for(int i = 0; i < splited.length; i++) {
            if(Objects.equals(splited[i], "episode")) {
                if(splited[i + 1].contains(".")) {
                    podcastId = Integer.valueOf(splited[i + 1].split("\\.")[0]);
                } else {
                    podcastId = Integer.valueOf(splited[i + 1]);
                }

            }
        }
        if(podcastId < 0) {
            LOG.info("Podcast id of file {} parsed incorrect", file.getName());
        }
        return podcastId;
    }
    private static int getDataByPattern(String filename, Pattern pattern) {
        List<String> splitedFileName = Arrays.asList(filename.split("-"));
        List<String> filtered = splitedFileName.stream().filter(data -> pattern.matcher(data).matches()).collect
                (Collectors.toList());
        if(filtered.isEmpty()) {
            return 0;
        } else {
            return Integer.valueOf(filtered.stream().findFirst().get());
        }
    }
}

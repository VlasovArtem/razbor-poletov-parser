package org.avlasov.razborpoletov.reader.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 05/08/15.
 */
public class PodcastFileUtils {
    private final static Logger LOGGER = LogManager.getLogger(PodcastFileUtils.class);

    public static LocalDate getPodcastDate(File file) {
        int year = getDataByPattern(file.getName(), Pattern.compile("201[0-9]"));
        int month = getDataByPattern(file.getName(), Pattern.compile("\b0\b|0[1-9]|1[0-2]"));
        int day = getDataByPattern(file.getName(), Pattern.compile("0[1-9]|[1-2][0-9]|3[0-1]"));
        if (year == 0 || month == 0 || day == 0) {
            LOGGER.info("File {} has no one of the data: year {}, month {}, day {}", file.getName(), year, month, day);
            return null;
        }
        return LocalDate.of(year, month, day);
    }

    public static Optional<Integer> getPodcastId(File file) {
        if (Objects.nonNull(file)) {
            Pattern compile = Pattern.compile("(?!.*(episode))\\d+(?=(-\\w+)*\\.\\w+)");
            Matcher matcher = compile.matcher(file.getName());
            if (matcher.find())
                return Optional.ofNullable(Integer.valueOf(matcher.group()));
            LOGGER.info("Podcast id of file {} parsed incorrect", file.getName());
        } else {
            LOGGER.warn("Podcast file is empty");
        }
        return Optional.empty();
    }

    public static List<Integer> getPodcastsId(List<File> files) {
        if (Objects.nonNull(files)) {
            return files
                    .stream()
                    .map(PodcastFileUtils::getPodcastId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static int[] getPodcastsIdArray(List<File> files) {
        return getPodcastsId(files)
                .stream()
                .mapToInt(value -> value)
                .toArray();
    }

    private static int getDataByPattern(String filename, Pattern pattern) {
        List<String> splitedFileName = Arrays.asList(filename.split("-"));
        List<String> filtered = splitedFileName.stream().filter(data -> pattern.matcher(data).matches()).collect
                (Collectors.toList());
        if (filtered.isEmpty()) {
            return 0;
        } else {
            return Integer.valueOf(filtered.stream().findFirst().get());
        }
    }
}

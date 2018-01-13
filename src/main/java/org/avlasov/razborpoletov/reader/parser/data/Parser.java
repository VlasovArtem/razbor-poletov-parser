package org.avlasov.razborpoletov.reader.parser.data;

import java.io.File;
import java.util.List;

/**
 * Created by artemvlasov on 12/06/15.
 */
public interface Parser<T> {

    String PODCAST_FILE_PATTERN = "20([0-9]{2}-){3}episode-[0-9].+";

    List<T> parse(List<File> files);
    List<T> parse(File file);

}

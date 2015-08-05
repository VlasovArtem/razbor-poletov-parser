package razborpoletov.reader.parsers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by artemvlasov on 12/06/15.
 */
public interface Parser {
    List<?> parse(List<File> files, boolean asciidocOnly) throws IOException,
            URISyntaxException;
    List<?> parse(File file) throws IOException, URISyntaxException;
    List<?> parseAsciidoc(File file) throws IOException, URISyntaxException;
}

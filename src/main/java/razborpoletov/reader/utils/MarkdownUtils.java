package razborpoletov.reader.utils;

import org.markdown4j.Markdown4jProcessor;

import java.io.File;
import java.io.IOException;

/**
 * Created by artemvlasov on 26/04/15.
 */
public class MarkdownUtils {
    public static String parseToHtml(File file) throws IOException {
        String html = new Markdown4jProcessor().process(file);
        return html;
    }
}

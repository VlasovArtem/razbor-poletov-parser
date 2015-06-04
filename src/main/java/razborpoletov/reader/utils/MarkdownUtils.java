package razborpoletov.reader.utils;

import org.markdown4j.Markdown4jProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 26/04/15.
 */
public class MarkdownUtils {
    public static String parseToHtml(File file) throws IOException {
        String html = new Markdown4jProcessor().process(file);
        return html;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/artemvlasov/git/razbor-poletov.github.com/source/_posts/2012-05-22-episode-18" +
                ".markdown");
        String title = null;
        String date = null;
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            int countBasicElement = 0;
            for(String line; (line = br.readLine()) != null; ) {
                if(line.equals("---")) {
                    countBasicElement++;
                }
                if(countBasicElement == 1) {
                    if(Pattern.compile("title: .+").matcher(line).matches()) {
                        title = line;
                    } else if(Pattern.compile("date: .+").matcher(line).matches()) {
                        date = line;
                    }
                } else if(countBasicElement > 1) {
                    break;
                }
            }
        }
        System.out.println("Title: = " + title.split(" ", 2)[1].replaceAll("\"", "") + "\nDate: = " + date.split(" ", 2)
                [1]);
    }
}

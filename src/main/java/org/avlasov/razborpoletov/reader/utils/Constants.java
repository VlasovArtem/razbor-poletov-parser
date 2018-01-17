package org.avlasov.razborpoletov.reader.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by artemvlasov on 22/05/15.
 */
public class Constants {
    public final static String MARKDOWN_FORMAT = "markdown";
    public final static String ASCII_DOC = "adoc";
    public final static String HTML = "html";
    public final static String DUPLICATE_TAGS = "/duplicateTags.json";
    public final static String TAGS = "/tags.json";
    public final static List<String> PROTOCOLS = Arrays.asList("http://", "https://");
    public final static List<String> IGNORED_URLS = Arrays.asList("http://www.latencytop.org/", "http://tobacco" +
            ".noroutine.me", "http://www.fossil-scm.org/index.html/doc/tip/www/quickstart.wiki");
}

package razborpoletov.reader.utils;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.StructuredDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 26/04/15.
 */
public class AsciidocUtils {
    private static final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
    private static final Logger LOG = LoggerFactory.getLogger(AsciidocUtils.class);
    private static final String TWITTER_PART_NAME = "_twitter";
    private static final List<String> DOCUMENT_IDS = Arrays.asList(".?полезняшк.?", ".?конференци.?");


    public static Document parseTwitterPart(File file) {
        StructuredDocument document = asciidoctor.readDocumentStructure(file, new HashMap<>());
        ContentPart part = document.getPartById(TWITTER_PART_NAME);
        String podcastName = file.getName();
        if(part == null) {
            LOG.info("Document {} has no twitter part", podcastName);
        }
        return part == null ? null : Jsoup.parse(part.getContent());
    }

    public static Document parsePartById(File file, String partId) {
        if(Objects.isNull(partId)) {
            throw new NullPointerException("part id cannot be null");
        }
        Optional<String> id = DOCUMENT_IDS.stream().filter(docId -> matchText(docId, partId)).findFirst();
        String patternId = id.orElseThrow(() -> new IllegalArgumentException("Incorrect part id"));
        List<ContentPart> parts = asciidoctor.readDocumentStructure(file, new HashMap<>()).getParts();
        ContentPart part = null;
        Optional<ContentPart> contentPart = parts.stream()
                .filter(filePart -> Objects.nonNull(filePart.getId()) &&
                        matchText(patternId, filePart.getId()))
                .findFirst();
        if(contentPart.isPresent()) {
            part = contentPart.get();
        }
        String podcastName = file.getName();
        if(part == null) {
            LOG.info("Document {} has no {} part", podcastName, partId);
        }
        return part == null ? null : Jsoup.parse(part.getContent());
    }

    public static boolean matchText(String pattern, String text) {
        return Objects.nonNull(pattern) &&
                Objects.nonNull(text) &&
                Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text).matches();
    }
}

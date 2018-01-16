package org.avlasov.razborpoletov.reader.utils;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.StructuredDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 26/04/15.
 */
public class AsciidocUtils {

    private static final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
    private static final Logger LOG = LoggerFactory.getLogger(AsciidocUtils.class);
    private static final List<String> DOCUMENT_IDS = Arrays.asList(".?полезняшк.?", ".?конференци.?");


    public static StructuredDocument parseDocument(File file) {
        return asciidoctor.readDocumentStructure(file, new HashMap<>());
    }

    public static List<Document> parseTwitterPart(File file) {
        StructuredDocument document = asciidoctor.readDocumentStructure(file, new HashMap<>());
        return document
                .getParts()
                .stream()
                .filter(p -> p.getContent().contains("twitter"))
                .map(contentPart -> Jsoup.parse(contentPart.getContent()))
                .collect(Collectors.toList());
    }

    public static Optional<Element> parsePartById(File file, String partId) {
        Objects.requireNonNull(partId, "part id cannot be null");
        String id = DOCUMENT_IDS.stream()
                .filter(docId -> matchText(docId, partId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Incorrect part id"));
        List<ContentPart> parts = asciidoctor.readDocumentStructure(file, new HashMap<>()).getParts();
        Optional<ContentPart> contentPart = parts.stream()
                .filter(cp -> Objects.nonNull(cp.getId()) &&
                        matchText(id, cp.getId()))
                .findFirst();
        if (contentPart.isPresent()) {
            return Optional.ofNullable(Jsoup.parse(contentPart.get().getContent()));
        } else {
            Optional<Element> requiredDocumentPart = parts
                    .stream()
                    .map(data -> findContentPartWithId(data, id))
                    .filter(Objects::nonNull)
                    .findFirst();
            if (!requiredDocumentPart.isPresent())
                LOG.info("Document {} has no {} part", file.getName(), partId);
            return requiredDocumentPart;
        }
    }

    private static boolean matchText(String pattern, String text) {
        return Objects.nonNull(pattern) &&
                Objects.nonNull(text) &&
                Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text).matches();
    }

    private static Element findContentPartWithId(ContentPart contentPart, String patternId) {
        Document parse = Jsoup.parse(contentPart.getContent());
        Elements data = Collector.collect(new MatchesId(patternId), parse);
        if (!data.isEmpty()) {
            Element element = data.get(0);
            if (element.tag().getName().matches("h[1-6]")) {
                return element.parent();
            }
            return element;
        }
        return null;
    }

    private final static class MatchesId extends Evaluator {
        private String patternId;

        MatchesId(String patternId) {
            this.patternId = patternId;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return matchText(patternId, element.id());
        }

        @Override
        public String toString() {
            return String.format("#%s", patternId);
        }
    }


}

package org.avlasov.razborpoletov.reader.utils;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.StructuredDocument;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 16/01/2018
 **/
@PrepareForTest({AsciidocUtils.class, Asciidoctor.Factory.class, Jsoup.class, Collector.class, Document.class})
public class AsciidocUtilsTest extends PowerMockitoTestCase {

    @Mock
    private StructuredDocument structuredDocument;
    @Mock
    private ContentPart contentPart;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Document document;

    @Before
    public void setUp() throws Exception {
        Asciidoctor asciidoctor = mock(Asciidoctor.class);
        Whitebox.setInternalState(AsciidocUtils.class, "asciidoctor", asciidoctor);
        mockStatic(Asciidoctor.Factory.class, Jsoup.class, Collector.class);
        when(asciidoctor.readDocumentStructure(any(File.class), anyMapOf(String.class, Object.class))).thenReturn(structuredDocument);
        when(Asciidoctor.Factory.create()).thenReturn(asciidoctor);
        when(Jsoup.parse(anyString())).thenReturn(document);
        when(structuredDocument.getParts()).thenReturn(Collections.singletonList(contentPart));
    }

    @Test
    public void parseDocument_WithValidData_ReturnStructuredDocument() {
        StructuredDocument structuredDocument = AsciidocUtils.parseDocument(new File(""));
        assertNotNull(structuredDocument);
    }

    @Test
    public void parseTwitterPart_WithExistingTwitterParts_ReturnDocumentCollection() {
        when(contentPart.getContent()).thenReturn("twitter");
        List<Document> documents = AsciidocUtils.parseTwitterPart(new File(""));
        assertThat(documents, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void parseTwitterPart_WithoutExistingTwitterParts_ReturnEmptyCollection() {
        when(contentPart.getContent()).thenReturn("hello");
        List<Document> documents = AsciidocUtils.parseTwitterPart(new File(""));
        assertThat(documents, IsEmptyCollection.empty());
    }

    @Test
    public void parsePartById_WithValidData_ReturnOptionalElement() {
        when(contentPart.getId()).thenReturn("полезняшки");
        when(contentPart.getContent()).thenReturn("test");
        Optional<Element> element = AsciidocUtils.parsePartById(new File(""), "полезняшки");
        assertTrue(element.isPresent());
    }

    @Test(expected = NullPointerException.class)
    public void parsePartById_WithNullPartId_ThrowException() {
        AsciidocUtils.parsePartById(new File(""), null);
    }

    @Test
    public void parsePartById_WithNullContentPartId_ReturnElement() {
        when(document.parent()).thenReturn(document);
        when(contentPart.getId()).thenReturn(null);
        when(Collector.collect(any(Evaluator.class), any(Element.class))).thenReturn(new Elements(document));
        when(document.tag().getName()).thenReturn("h1");
        Optional<Element> element = AsciidocUtils.parsePartById(new File(""), "полезняшки");
        assertTrue(element.isPresent());
    }

    @Test
    public void parsePartById_WithNullContentPartIdAndNotMatchesTagName_ReturnElement() {
        when(contentPart.getId()).thenReturn(null);
        when(Collector.collect(any(Evaluator.class), any(Element.class))).thenReturn(new Elements(document));
        when(document.tag().getName()).thenReturn("hello");
        Optional<Element> element = AsciidocUtils.parsePartById(new File(""), "полезняшки");
        assertTrue(element.isPresent());
    }

    @Test
    public void parsePartById_WithNullContentPartIdAndNotMatchesElements_ReturnElement() {
        when(contentPart.getId()).thenReturn(null);
        when(Collector.collect(any(Evaluator.class), any(Element.class))).thenReturn(new Elements());
        Optional<Element> element = AsciidocUtils.parsePartById(new File(""), "полезняшки");
        assertFalse(element.isPresent());
    }

    @Test
    public void MatchesIdClassTest() throws Exception {
        Evaluator matchesId = (Evaluator) Whitebox.invokeConstructor(Whitebox.getInnerClassType(AsciidocUtils.class, "MatchesId"), ".*");
        when(document.id()).thenReturn("test");
        assertTrue(matchesId.matches(document, document));
        assertNotNull(matchesId.toString());
    }
}
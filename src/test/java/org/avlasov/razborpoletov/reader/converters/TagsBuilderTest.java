package org.avlasov.razborpoletov.reader.converters;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class TagsBuilderTest {

    @Test
    public void basicAsciidocElementBuilder() {
        assertNotNull(TagsBuilder.basicAsciidocElementBuilder(1, 2, "test", "test", "test"));
    }

    @Test
    public void audioTagHtmlBuilder() {
        assertNotNull(TagsBuilder.audioTagHtmlBuilder("test"));
    }

    @Test
    public void audioTagAsciidocBuilder() {
        assertNotNull(TagsBuilder.audioTagAsciidocBuilder("test"));
    }

    @Test
    public void downloadTagHtmlBuilder() {
        assertNotNull(TagsBuilder.downloadTagHtmlBuilder("Test"));
    }

    @Test
    public void downloadTagAsciidocBuilder() {
        assertNotNull(TagsBuilder.downloadTagAsciidocBuilder("hello"));
    }

    @Test
    public void imageTagHtmlBuilder() {
        assertNotNull(TagsBuilder.imageTagHtmlBuilder("Tset"));
    }

    @Test
    public void imageTagAsciidocBuilder() {
        assertNotNull(TagsBuilder.imageTagAsciidocBuilder("test"));
    }
}
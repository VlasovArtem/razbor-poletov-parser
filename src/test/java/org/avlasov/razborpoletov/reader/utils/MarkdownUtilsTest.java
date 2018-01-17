package org.avlasov.razborpoletov.reader.utils;

import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.junit.Before;
import org.junit.Test;
import org.markdown4j.Markdown4jProcessor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@PrepareForTest({MarkdownUtils.class})
public class MarkdownUtilsTest extends PowerMockitoTestCase {

    @Mock
    private Markdown4jProcessor processor;

    @Before
    public void setUp() throws Exception {
        whenNew(Markdown4jProcessor.class).withAnyArguments().thenReturn(processor);
        when(processor.process(any(File.class))).thenReturn("Test");
    }

    @Test
    public void parseToHtml_WithValidData_ReturnString() throws Exception {
        String test = MarkdownUtils.parseToHtml(new File("test"));
        assertEquals("Test", test);
    }

    @Test(expected = IOException.class)
    public void parseToHtml_WithParseThrowException_ThrownException() throws Exception {
        when(processor.process(any(File.class))).thenThrow(new IOException());
        MarkdownUtils.parseToHtml(new File("test"));
    }

}
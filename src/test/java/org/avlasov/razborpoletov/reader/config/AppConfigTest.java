package org.avlasov.razborpoletov.reader.config;

import org.avlasov.razborpoletov.reader.utils.CLIUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Created By artemvlasov on 19/01/2018
 **/
@RunWith(MockitoJUnitRunner.class)
public class AppConfigTest {

    @Test
    public void objectMapper() {
        AppConfig appConfig = new AppConfig();
        assertNotNull(appConfig.objectMapper());
    }

    @Test
    public void commandLine() {
        AppConfig appConfig = new AppConfig();
        assertNotNull(appConfig.commandLine(mock(CLIUtils.class, Answers.RETURNS_DEEP_STUBS.get())));
    }

}
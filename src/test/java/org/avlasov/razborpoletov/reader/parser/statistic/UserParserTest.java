package org.avlasov.razborpoletov.reader.parser.statistic;

import org.avlasov.razborpoletov.reader.parser.data.UserParser;
import org.avlasov.razborpoletov.reader.twitter.TwitterAPI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created By artemvlasov on 04/01/2018
 **/
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = UserParser.class)
public class UserParserTest {

    @Mock
    private TwitterAPI twitterAPI;
    @InjectMocks
    private UserParser parser;

    @Test
    public void parserTwitterAccountInformation() {
    }
}
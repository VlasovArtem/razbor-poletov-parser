package org.avlasov.razborpoletov.reader.utils;

import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class ConstantsTest {

    @Test
    public void validateProtocolsConstants() {
        assertThat(Constants.PROTOCOLS, IsCollectionContaining.hasItem("https://"));
    }

    @Test
    public void validateIgnoredUrlConstants() {
        assertThat(Constants.IGNORED_URLS, IsCollectionContaining.hasItem("http://www.latencytop.org/"));
    }
}
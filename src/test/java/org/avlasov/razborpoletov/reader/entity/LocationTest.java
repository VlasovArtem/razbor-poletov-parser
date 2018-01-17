package org.avlasov.razborpoletov.reader.entity;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class LocationTest {

    @Test
    public void builder() {
        assertNotNull(Location.builder().build());
    }
}
package org.avlasov.razborpoletov.reader.service.data.impl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created By artemvlasov on 18/01/2018
 **/
public class UsefulThingsServiceDataImplTest {

    @Test
    public void getFileName() {
        assertTrue("useful-things".equals(new UsefulThingsServiceDataImpl(null, null).getFileName()));
    }

}
package org.avlasov.razborpoletov.reader.service.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.avlasov.razborpoletov.reader.entity.info.Conference;
import org.avlasov.razborpoletov.reader.parser.data.ConferenceParser;
import org.avlasov.razborpoletov.reader.service.data.ServiceDataAbstract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created By artemvlasov on 05/01/2018
 **/
@Service
public class ConferenceServiceDataImpl extends ServiceDataAbstract<Conference, ConferenceParser> {

    @Autowired
    public ConferenceServiceDataImpl(ConferenceParser parser, ObjectMapper objectMapper) {
        super(parser, objectMapper);
    }

    @Override
    public String getFileName() {
        return "conferences";
    }

}

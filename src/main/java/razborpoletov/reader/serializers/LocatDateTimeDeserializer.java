package razborpoletov.reader.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Created by artemvlasov on 25/04/15.
 */
public class LocatDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if(p == null) {
            return null;
        }

        System.out.println(p.getText());
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(p.getText())), ZoneId
                .systemDefault());
    }
}

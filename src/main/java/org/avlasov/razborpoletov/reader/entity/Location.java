package org.avlasov.razborpoletov.reader.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;

/**
 * Created by artemvlasov on 24/04/15.
 */
@JsonAutoDetect
@Builder
public class Location {
    private String country;
    private String city;
    private String street;
    private String nameOfConferenceHall;
}

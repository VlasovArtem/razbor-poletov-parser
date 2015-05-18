package razborpoletov.reader.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Created by artemvlasov on 24/04/15.
 */
@JsonAutoDetect
public class Location {
    private String country;
    private String city;
    private String street;
    private String nameOfConferenceHall;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNameOfConferenceHall() {
        return nameOfConferenceHall;
    }

    public void setNameOfConferenceHall(String nameOfConferenceHall) {
        this.nameOfConferenceHall = nameOfConferenceHall;
    }
}

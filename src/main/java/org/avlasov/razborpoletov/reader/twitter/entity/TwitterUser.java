package org.avlasov.razborpoletov.reader.twitter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Objects;

/**
 * Created By artemvlasov on 03/01/2018
 **/
@Builder
public class TwitterUser implements Comparable<TwitterUser> {

    private long id;
    private String name;
    @JsonProperty(value = "screen_name")
    private String screenName;
    private String location;
    private String description;
    @JsonProperty(value = "profile_image_url")
    private String profileImageUrl;

    public TwitterUser() {}

    private TwitterUser(long id, String name, String screenName, String location, String description, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.screenName = screenName;
        this.location = location;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwitterUser)) return false;
        TwitterUser that = (TwitterUser) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(TwitterUser o) {
        return Long.compare(id, o.id);
    }
}

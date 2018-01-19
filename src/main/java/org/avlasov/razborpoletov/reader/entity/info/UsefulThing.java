package org.avlasov.razborpoletov.reader.entity.info;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by artemvlasov on 24/04/15.
 */
@JsonAutoDetect
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class UsefulThing {

    private String link;
    private String name;
    private List<String> tags;
    private boolean checked;
    private long podcastId;
    private String description;

    public String getLink() {
        return link;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return checked;
    }

    public long getPodcastId() {
        return podcastId;
    }

    public String getDescription() {
        return description;
    }
}

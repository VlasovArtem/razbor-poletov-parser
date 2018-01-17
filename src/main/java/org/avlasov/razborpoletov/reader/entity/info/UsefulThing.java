package org.avlasov.razborpoletov.reader.entity.info;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;

import java.util.List;

/**
 * Created by artemvlasov on 24/04/15.
 */
@JsonAutoDetect
@Builder
public class UsefulThing {

    private String link;
    private String name;
    private List<String> tags;
    private boolean checked;
    private long podcastId;
    private String description;

    public UsefulThing() {}

    private UsefulThing(String link, String name, List<String> tags, boolean checked, long podcastId, String description) {
        this.link = link;
        this.name = name;
        this.tags = tags;
        this.checked = checked;
        this.podcastId = podcastId;
        this.description = description;
    }

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

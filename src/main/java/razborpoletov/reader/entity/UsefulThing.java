package razborpoletov.reader.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

/**
 * Created by artemvlasov on 24/04/15.
 */
@JsonAutoDetect
public class UsefulThing {
    private String link;
    private String provider;
    private List<String> tags;
    private boolean checked;
    private long podcastId;
    private String description;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public long getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(long podcastId) {
        this.podcastId = podcastId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

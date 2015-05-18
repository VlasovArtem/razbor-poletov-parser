package razborpoletov.reader.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import razborpoletov.reader.serializers.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by artemvlasov on 24/04/15.
 */
@JsonAutoDetect
public class Podcast {
    private long id;
    private String name;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;
    private List<Conference> conferences;
    private List<UsefulThing> usefulStuffs;
    private boolean checked;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Conference> getConferences() {
        return conferences;
    }

    public void setConferences(List<Conference> conferences) {
        this.conferences = conferences;
    }

    public List<UsefulThing> getUsefulStuffs() {
        return usefulStuffs;
    }

    public void setUsefulStuffs(List<UsefulThing> usefulStuffs) {
        this.usefulStuffs = usefulStuffs;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}

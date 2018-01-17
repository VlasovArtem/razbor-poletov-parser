package org.avlasov.razborpoletov.reader.github.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@Builder
public class GithubProject {

    private long id;
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    private GithubProjectOwner owner;
    @JsonProperty("private")
    private boolean privateProject;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String description;
    private String language;

    public GithubProject() {}

    private GithubProject(long id, String name, String fullName, GithubProjectOwner owner, boolean privateProject, String htmlUrl, String description, String language) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.owner = owner;
        this.privateProject = privateProject;
        this.htmlUrl = htmlUrl;
        this.description = description;
        this.language = language;
    }

    public String getDescription() {
        return description;
    }
}

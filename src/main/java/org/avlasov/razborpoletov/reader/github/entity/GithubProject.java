package org.avlasov.razborpoletov.reader.github.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
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

    public String getDescription() {
        return description;
    }
}

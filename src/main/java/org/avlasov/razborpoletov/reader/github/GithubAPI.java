package org.avlasov.razborpoletov.reader.github;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.github.entity.GithubProject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@Component
@PropertySource("classpath:github.properties")
public class GithubAPI {

    private static final Logger LOGGER = LogManager.getLogger(GithubAPI.class);
    @Value("${token}")
    private String githubAPIToken;
    private final HttpHeaders tokenHeaders;
    private final RestTemplate restTemplate;

    public GithubAPI() {
        this.restTemplate = new RestTemplate();
        tokenHeaders = new HttpHeaders();
        tokenHeaders.add("Authorization", githubAPIToken);
    }

    public Optional<GithubProject> getGithubProject(String owner, String project) {
        if (Objects.toString(owner, "").isEmpty() || Objects.toString(project, "").isEmpty()) {
            LOGGER.warn("Owner or project argument is empty");
            return Optional.empty();
        }
        UriComponents build = UriComponentsBuilder.fromHttpUrl("https://api.github.com/repos")
                .pathSegment(owner, project)
                .build();
        ResponseEntity<GithubProject> githubProject = restTemplate.exchange(build.toUri(), HttpMethod.GET, new HttpEntity<>(tokenHeaders), GithubProject.class);
        if (!githubProject.getStatusCode().is2xxSuccessful()) {
            LOGGER.warn("Github API return warning on owner - {} and project - {}. Result status: {}.", owner, project, githubProject.getStatusCode());
            return Optional.empty();
        }
        return Optional.of(githubProject.getBody());
    }

    public Optional<GithubProject> getGithubProject(String githubUrl) {
        String githubPattern = "https?://github.com/.+/.+";
        if (Objects.isNull(githubUrl)) {
            LOGGER.warn("Github url is empty");
            return Optional.empty();
        } else if (!githubUrl.matches(githubPattern)) {
            LOGGER.warn("Github is not matches pattern '{}'", githubPattern);
            return Optional.empty();
        }
        try {
            URL url = new URL(githubUrl);
            String urlPath = url.getPath();
            String[] pathVariables = urlPath.split("/");
            return getGithubProject(pathVariables[1], pathVariables[2]);
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }

}

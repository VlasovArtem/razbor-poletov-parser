package org.avlasov.razborpoletov.reader.github;

import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.github.entity.GithubProject;
import org.avlasov.razborpoletov.reader.github.entity.GithubProjectOwner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created By artemvlasov on 17/01/2018
 **/
@PrepareForTest({GithubAPI.class})
public class GithubAPITest extends PowerMockitoTestCase {

    @Mock
    private RestTemplate restTemplate;
    private GithubAPI githubAPI;

    @Before
    public void setUp() throws Exception {
        whenNew(RestTemplate.class).withAnyArguments().thenReturn(restTemplate);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(GithubProject.class)))
                .thenReturn(new ResponseEntity<>(getGithubProjectEntity(), HttpStatus.OK));
        githubAPI = new GithubAPI();
    }

    @Test
    public void getGithubProject_WithValidOwnerAndProjectData_ReturnGithubProjectOptional() {
        Optional<GithubProject> githubProject = githubAPI.getGithubProject("test", "test");
        assertTrue(githubProject.isPresent());
    }

    @Test
    public void getGithubProject_WithNullOwner_ReturnEmptyOptional() {
        Optional<GithubProject> githubProject = githubAPI.getGithubProject(null, "test");
        assertFalse(githubProject.isPresent());
    }

    @Test
    public void getGithubProject_WithNullProject_ReturnEmptyOptional() {
        Optional<GithubProject> githubProject = githubAPI.getGithubProject("test", null);
        assertFalse(githubProject.isPresent());
    }

    @Test
    public void getGithubProject_WithGithubAPIErrorResponse_ReturnEmptyOptional() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(GithubProject.class)))
                .thenReturn(new ResponseEntity<>(GithubProject.builder().build(), HttpStatus.FORBIDDEN));
        Optional<GithubProject> githubProject = githubAPI.getGithubProject("test", "test");
        assertFalse(githubProject.isPresent());
    }

    @Test
    public void getGithubProject_WithValidGithubUrl_ReturnGithubProjectOptional() {
        Optional<GithubProject> githubProject = githubAPI.getGithubProject("https://github.com/test/test");
        assertTrue(githubProject.isPresent());
    }

    @Test
    public void getGithubProject_WithNullGithubUrl_ReturnEmptyOptional() {
        Optional<GithubProject> githubProject = githubAPI.getGithubProject(null);
        assertFalse(githubProject.isPresent());
    }

    @Test
    public void getGithubProject_WithInvalidGithubUrl_ReturnEmptyOptional() {
        Optional<GithubProject> githubProject = githubAPI.getGithubProject("http://test.com");
        assertFalse(githubProject.isPresent());
    }

    @Test
    public void getGithubProject_WithURLThrowException_ReturnEmptyOptional() throws Exception {
        whenNew(URL.class).withArguments(anyString()).thenThrow(new MalformedURLException());
        Optional<GithubProject> githubProject = githubAPI.getGithubProject("https://github.com/test/test");
        assertFalse(githubProject.isPresent());
    }

    private GithubProject getGithubProjectEntity() {
        return GithubProject.builder()
                .description("Test Description")
                .fullName("Test Full Name")
                .htmlUrl("http://testhtmlurl.com")
                .id(56262)
                .language("Java")
                .name("Test name")
                .owner(new GithubProjectOwner(56526, "test login"))
                .build();
    }

}
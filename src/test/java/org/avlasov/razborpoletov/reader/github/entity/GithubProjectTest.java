package org.avlasov.razborpoletov.reader.github.entity;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created By artemvlasov on 18/01/2018
 **/
public class GithubProjectTest {

    @Test
    public void defaultConstructor() {
        new GithubProject();
    }

    @Test
    public void getDescription() {
        assertNotNull(GithubProject.builder().description("test").build().getDescription());
    }

    @Test
    public void builder() {
        GithubProject build = GithubProject.builder()
                .description("test")
                .owner(new GithubProjectOwner(10101, "test"))
                .name("test")
                .language("HTML")
                .id(454)
                .htmlUrl("hello")
                .fullName("test")
                .build();
        assertNotNull(build);
    }
}
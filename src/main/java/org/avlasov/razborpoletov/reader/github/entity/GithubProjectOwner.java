package org.avlasov.razborpoletov.reader.github.entity;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class GithubProjectOwner {

    private long id;
    private String login;

    public GithubProjectOwner(long id, String login) {
        this.id = id;
        this.login = login;
    }

}

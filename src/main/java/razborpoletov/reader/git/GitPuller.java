package razborpoletov.reader.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;

/**
 * Created by artemvlasov on 19/04/15.
 */
public class GitPuller {
    public static final String LOCAL_GIT_FOLDER_PROP_NAME = "local.git.folder";
    private Git git;

    public GitPuller(String gitLocalPath) throws IOException {
        Repository localRepo = new FileRepository(gitLocalPath + "/.git");
        git = new Git(localRepo);
    }

    public PullResult pull() throws GitAPIException {
        return git.pull().call();
    }
}

package org.avlasov.razborpoletov.reader.git;

import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 18/01/2018
 **/
@PrepareForTest({ GitPuller.class })
public class GitPullerTest extends PowerMockitoTestCase {

    @Mock
    private FileRepository fileRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Git git;

    @Before
    public void setUp() throws Exception {
        whenNew(FileRepository.class).withAnyArguments().thenReturn(fileRepository);
        whenNew(Git.class).withAnyArguments().thenReturn(git);
        when(git.pull().call()).thenReturn(mock(PullResult.class));
    }

    @Test
    public void pull() throws Exception {
        PullResult test = new GitPuller("test").pull();
        assertNotNull(test);
    }

    @Test(expected = GitAPIException.class)
    public void pull_GitThrowException_ThrownException() throws Exception {
        when(git.pull().call()).thenThrow(new InvalidConfigurationException("test"));
        new GitPuller("test").pull();
    }

    @Test(expected = IOException.class)
    public void withConstructor_ThrowException_ThrownException() throws Exception {
        whenNew(FileRepository.class).withArguments(anyString()).thenThrow(new IOException());
        new GitPuller("test");
    }
}
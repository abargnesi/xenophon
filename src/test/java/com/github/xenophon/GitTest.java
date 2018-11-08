package com.github.xenophon;

import com.github.xenophon.tools.Git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class GitTest {

    @Test
    public void gitHubRemote() throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repository = builder
            .findGitDir(new File("/home/tony/repos/forsight"))
            .build();

        final RemoteConfig githubRemote = Git.findGithubRemote(repository);
        System.out.println(githubRemote.getName() + ": " + githubRemote.getURIs());
    }
}

package com.github.xenophon;

import com.github.xenophon.tools.Git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class JavaTest {

    @Test
    public void gitTreeFilenames() throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repository = builder
            .findGitDir(new File("/home/tony/repos/forsight"))
            .build();

        final String base = "2bbff8^";
        final String head = "2bbff8";

        Git
            .fileDifferencesBetweenCommits(base, head, repository)
            .forEach(diff -> System.out.println(diff.toString()));
    }
}

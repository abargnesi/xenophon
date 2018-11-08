package com.github.xenophon;

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

class GitLogCommitsTest {

    @Test
    void listCommits() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repo = builder
            .findGitDir(new File("/home/tony/repos/forsight"))
            .build();

        final Stream<RevCommit> revisions = revisions(
            repo.parseCommit(repo.resolve("RELEASE-144")),
            repo.parseCommit(repo.resolve("RELEASE-143")),
            repo);

        revisions.forEach(commit -> System.out.println(commit.getFullMessage()));
    }

    static Stream<RevCommit> revisions(final RevCommit latest, final RevCommit earliest, final Repository repo)
        throws IOException {

        try (RevWalk rw = new RevWalk(repo)) {
            rw.markStart(rw.lookupCommit(latest));
            rw.markUninteresting(rw.lookupCommit(earliest));

            return stream(spliteratorUnknownSize(rw.iterator(), ORDERED | NONNULL), false);
        }
    }
}

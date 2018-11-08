package com.github.xenophon;

import com.github.xenophon.model.ExtendedPullRequest;
import com.github.xenophon.tools.Git;
import com.github.xenophon.tools.GitHub;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class SummarizeTest {

    @Test
    public void showPrDifferences() throws IOException {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token("6fa5ddd33f1419f2a591cfdc84bceb6ec4d1f259");

        final RepositoryId xenophonRepoId = new RepositoryId("abargnesi", "xenophon");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repository = builder
            .findGitDir(new File("/home/tony/repos/xenophon"))
            .build();

        final ExtendedPullRequest pr = GitHub.pullRequest(client, xenophonRepoId, 1356);

        Git
            .fileDifferencesBetweenCommits(pr.getBase().getSha(), pr.getHead().getSha(), repository)
            .forEach(diff -> {
                System.out.println(diff.getDiff().getNewPath());
                System.out.println(diff.getPatch());
            });
    }
}

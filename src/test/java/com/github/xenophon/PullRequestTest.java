package com.github.xenophon;

import static java.time.Instant.ofEpochSecond;
import static java.time.LocalDateTime.ofInstant;

import com.github.xenophon.data.PullRequests;
import com.github.xenophon.model.ExtendedPullRequest;
import com.github.xenophon.model.Project;
import com.github.xenophon.tools.Git;
import com.github.xenophon.tools.GitHub;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.stream.Stream;

class PullRequestTest {

    @Test
    void mergedPullRequests() throws IOException {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token("6fa5ddd33f1419f2a591cfdc84bceb6ec4d1f259");

        final RepositoryId xenophoneRepoId = new RepositoryId("abargnesi", "xenophon");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository gitRepository = builder
            .findGitDir(new File("/home/tony/repos/xenophon"))
            .build();

        PullRequests
            .mergedPullRequestsLastThirtyDays(
                client, new Project(xenophoneRepoId, gitRepository),
                "RELEASE-144", "RELEASE-143")
            .getPullRequests()
            .forEach(pr -> System.out.println(pr.getNumber() + " >> " + pr.getTitle()));
    }

    @Test
    void listPullRequests() throws IOException {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token("6fa5ddd33f1419f2a591cfdc84bceb6ec4d1f259");

        final RepositoryId xenophoneRepoId = new RepositoryId("abargnesi", "xenophon");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository gitRepository = builder
            .findGitDir(new File("/home/tony/repos/xenophon"))
            .build();

        {
            final Stream<PullRequest> pullRequests = GitHub.openPullRequests(client, xenophoneRepoId);
            pullRequests.forEach(pr -> {
                System.out.println(pr.getTitle());

                try {
                    final RemoteConfig githubRemote = Git.findGithubRemote(gitRepository);
                    Git.fetchFromRemote(githubRemote, gitRepository);
                    Git.getOrCreateBranchFromRemote(pr.getHead().getRef(), githubRemote, gitRepository);

                    final RevCommit latestCommit = Git.findCommit(pr.getHead().getRef(), gitRepository);
                    System.out.println("  " + latestCommit);
                    System.out.println("  " + ofInstant(ofEpochSecond(latestCommit.getCommitTime()), ZoneId.of("America/New_York")));
                } catch (IOException | GitAPIException e) {
                    e.printStackTrace();
                }
            });
        }

        {
            final Stream<PullRequest> pullRequests = GitHub.closedPullRequests(client, xenophoneRepoId);
            pullRequests.forEach(pr -> {
                final ExtendedPullRequest extendedPullRequest = GitHub.pullRequest(client, xenophoneRepoId, pr.getNumber());
                System.out.println(extendedPullRequest.getMergeCommitSha());

                System.out.println(pr.getTitle());
                System.out.println("  head: " + pr.getHead().getLabel());
                System.out.println("  base: " + pr.getBase().getLabel());
            });
        }
    }

    @Test
    void getPullRequest() throws IOException {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token("6fa5ddd33f1419f2a591cfdc84bceb6ec4d1f259");

        final RepositoryId xenophoneRepoId = new RepositoryId("abargnesi", "xenophon");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository gitRepository = builder
            .findGitDir(new File("/home/tony/repos/xenophon"))
            .build();

        final ExtendedPullRequest pr = GitHub.pullRequest(client, xenophoneRepoId, 1356);
        System.out.println(pr.getMergeCommitSha());
    }
}

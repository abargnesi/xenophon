package com.github.xenophon.tools;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_PULLS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import com.github.xenophon.model.ExtendedPullRequest;
import com.github.xenophon.model.Project;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

@UtilityClass
public class GitHub {

    public Stream<ExtendedPullRequest> mergedPullRequests(
        final GitHubClient client, final Project project,
        final String latest, final String earliest,
        final Predicate<PullRequest> takeWhilePredicate) {

        final Predicate<PullRequest> prTakeWhile = ofNullable(takeWhilePredicate).orElse(pr -> true);

        return closedPullRequests(client, project.getGithubRepositoryId())
            .filter(pr -> nonNull(pr.getMergedAt()))
            .takeWhile(prTakeWhile)
            .map(pr -> GitHub.pullRequest(client, project.getGithubRepositoryId(), pr.getNumber()))
            .filter(extended -> {
                try {
                    final Repository r = project.getGitRepository();

                    return Git
                        .commitsBetween(r.parseCommit(r.resolve(latest)), r.parseCommit(r.resolve(earliest)), r)
                        .anyMatch(commit -> commit.name().equals(extended.getMergeCommitSha()));
                } catch (IOException e) {
                    return false;
                }
            });
    }

    public Stream<PullRequest> closedPullRequests(final GitHubClient client, final RepositoryId repositoryId) {
        return pullRequests(client, repositoryId, "closed");
    }

    public Stream<PullRequest> openPullRequests(final GitHubClient client, final RepositoryId repositoryId) {
        return pullRequests(client, repositoryId, "open");
    }

    public ExtendedPullRequest pullRequest(final GitHubClient client, final RepositoryId repositoryId, final int pullRequestNumber) {
        final String repoId = repositoryId.generateId();
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repoId);
        uri.append(SEGMENT_PULLS);
        uri.append('/').append(pullRequestNumber);

        GitHubRequest request = new GitHubRequest();
        request.setUri(uri);
        request.setType(ExtendedPullRequest.class);
        try {
            return (ExtendedPullRequest) client.get(request).getBody();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving pull request.", e);
        }
    }

    public Stream<RepositoryCommit> pullRequestCommits(final GitHubClient client, final RepositoryId repositoryId, final int pullRequestNumber) {
        final PullRequestService prService = new PullRequestService(client);
        try {
            return prService.getCommits(repositoryId, pullRequestNumber).stream();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving pull request commits.");
        }
    }

    public Stream<Pair<CommitFile, Blob>> pullRequestChangedFiles(final GitHubClient client, final RepositoryId repositoryId, final int pullRequestNumber) {
        final PullRequestService prService = new PullRequestService(client);
        final DataService gitDataService   = new DataService(client);
        try {
            return prService
                .getFiles(repositoryId, pullRequestNumber)
                .stream()
                .map(commitFile -> {
                    final String blobSHA = commitFile.getSha();
                    try {
                        return Pair.of(commitFile, gitDataService.getBlob(repositoryId, blobSHA));
                    } catch (IOException e) {
                        throw new RuntimeException("Error retrieving pull request changed files.");
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving pull request changed files.");
        }
    }

    public Stream<CommitFile> commitChangedFiles(final GitHubClient client, final RepositoryId repositoryId, final RepositoryCommit commit) {
        final CommitService commitService = new CommitService(client);
        try {
            return commitService.getCommit(repositoryId, commit.getSha()).getFiles().stream();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving pull request changed files.");
        }
    }

    private Stream<PullRequest> pullRequests(final GitHubClient client, final RepositoryId repositoryId, final String state) {
        final PullRequestService prService = new PullRequestService(client);
        final PageIterator<PullRequest> prPageIterator = prService.pagePullRequests(repositoryId, state);

        return stream(spliteratorUnknownSize(prPageIterator.iterator(), NONNULL), false)
            .flatMap(Collection::stream);
    }
}

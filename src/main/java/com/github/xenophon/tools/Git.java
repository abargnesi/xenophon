package com.github.xenophon.tools;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK;

import com.github.xenophon.model.DiffEntryWithPatch;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

@UtilityClass
public class Git {

    public RemoteConfig findGithubRemote(
        @NonNull final Repository repository)
        throws GitAPIException {

        final var git = porcelainAPI(repository);
        return git.remoteList().call().stream()
            .filter(remote ->
                remote.getURIs().stream()
                    .anyMatch(uri -> uri.getHost().equals("github.com")))
            .findFirst()
            .orElse(null);
    }

    public FetchResult fetchFromRemote(
        @NonNull final RemoteConfig remote,
        @NonNull final Repository repository)
        throws GitAPIException {

        final var git = porcelainAPI(repository);
        return git.fetch()
            .setRemote(remote.getName())
            .setRefSpecs(remote.getFetchRefSpecs())
            .call();
    }

    public Ref getOrCreateBranchFromRemote(
        @NonNull final String name,
        @NonNull final RemoteConfig remote,
        @NonNull final Repository repository)
        throws GitAPIException {

        final var git = porcelainAPI(repository);

        try {
            return git.branchCreate()
                .setName(name)
                .setStartPoint(remote.getName() + "/" + name)
                .setUpstreamMode(TRACK)
                .call();
        } catch (RefAlreadyExistsException excp) {
            // ignore
            return git.branchList().call().stream()
                .filter(ref -> ref.getName().equals(name))
                .findFirst()
                .orElse(null);
        }
    }

    public RevCommit findCommit(
        @NonNull final PullRequest pullRequest,
        @NonNull final Repository repository)
        throws IOException {

        return findCommit(pullRequest.getHead().getRef(), repository);
    }

    public RevCommit findCommit(
        @NonNull final String commitReference,
        @NonNull final Repository repository)
        throws IOException {

        return repository.parseCommit(repository.resolve(commitReference));
    }

    public Stream<RevCommit> commitsBetween(
        @NonNull final String base,
        @NonNull final String head,
        @NonNull final Repository repository) {

        try {
            return commitsBetween(
                repository.parseCommit(repository.resolve(base)),
                repository.parseCommit(repository.resolve(head)),
                repository);
        } catch (IOException excp) {
            throw new RuntimeException("Error retrieving commits.", excp);
        }
    }

    public Stream<RevCommit> commitsBetween(
        @NonNull final RevCommit base,
        @NonNull final RevCommit head,
        @NonNull final Repository repository) {

        try (RevWalk rw = new RevWalk(repository)) {
            rw.markStart(base);
            rw.markUninteresting(head);
            return stream(spliteratorUnknownSize(rw.iterator(), ORDERED | NONNULL), false);
        } catch (IOException excp) {
            throw new RuntimeException("Error retrieving commits.", excp);
        }
    }

    public Stream<DiffEntryWithPatch> fileDifferencesBetweenCommits(
        @NonNull final String base,
        @NonNull final String head,
        @NonNull final Repository repository) {

        try (final RevWalk revWalk = new RevWalk(repository)) {
            final RevCommit baseCommit = revWalk.parseCommit(repository.resolve(base));
            final RevCommit headCommit = revWalk.parseCommit(repository.resolve(head));
            return fileDifferencesBetweenCommits(baseCommit, headCommit, repository);
        } catch (IOException e) {
            throw new RuntimeException("Error computing file differences between commits.", e);
        }
    }

    public Stream<DiffEntryWithPatch> fileDifferencesBetweenCommits(
        @NonNull final RevCommit base,
        @NonNull final RevCommit head,
        @NonNull final Repository repository) {

        try (final RevWalk revWalk = new RevWalk(repository)) {
            final RevTree baseTree = revWalk.parseTree(base.getTree());
            final RevTree headTree = revWalk.parseTree(head.getTree());

            try (ObjectReader reader = repository.newObjectReader()) {
                final CanonicalTreeParser baseTreeIterator = new CanonicalTreeParser();
                baseTreeIterator.reset(reader, baseTree);

                final CanonicalTreeParser headTreeIterator = new CanonicalTreeParser();
                headTreeIterator.reset(reader, headTree);

                try (
                    final ByteArrayOutputStream bytesPatch   = new ByteArrayOutputStream(32_768);
                    final BufferedOutputStream bufferedPatch = new BufferedOutputStream(bytesPatch)) {

                    final DiffFormatter diffFormatter = new DiffFormatter(bufferedPatch);
                    diffFormatter.setRepository(repository);
                    diffFormatter.setContext(0);
                    diffFormatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);

                    return diffFormatter
                        .scan(baseTreeIterator, headTreeIterator)
                        .stream()
                        .map(entry -> {
                            try {
                                diffFormatter.format(entry);
                                bufferedPatch.flush();

                                return new DiffEntryWithPatch(entry, bytesPatch.toString(UTF_8));
                            } catch (IOException e) {
                                throw new RuntimeException("Error formatting diff entry.", e);
                            }
                        });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error computing file differences between commits.", e);
        }
    }

    public Stream<DiffEntryWithPatch> fileDifferencesInRevision(
        @NonNull final String revisionReference,
        @NonNull final Repository repository) {

        try (final RevWalk revWalk = new RevWalk(repository)) {
            final RevCommit baseCommit = revWalk.parseCommit(repository.resolve(Strings.gitParent(revisionReference)));
            final RevCommit headCommit = revWalk.parseCommit(repository.resolve(revisionReference));
            return fileDifferencesBetweenCommits(baseCommit, headCommit, repository);
        } catch (IOException e) {
            throw new RuntimeException("Error computing file differences between commits.", e);
        }
    }

    org.eclipse.jgit.api.Git porcelainAPI(
        @NonNull final Repository repository) {

        return new org.eclipse.jgit.api.Git(repository);
    }
}

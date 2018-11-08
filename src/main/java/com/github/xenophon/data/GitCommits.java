package com.github.xenophon.data;

import static java.util.stream.Collectors.toList;

import com.github.xenophon.model.GitCommitHistory;
import com.github.xenophon.model.Project;
import com.github.xenophon.tools.Git;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GitCommits {

    public GitCommitHistory gitCommitHistory(
        final Project project,
        final String latest,
        final String earliest) {

        return new GitCommitHistory(
            Git
                .commitsBetween(latest, earliest, project.getGitRepository())
                .collect(toList()));
    }
}

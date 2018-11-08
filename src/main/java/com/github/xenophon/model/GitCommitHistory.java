package com.github.xenophon.model;

import static java.util.Collections.unmodifiableList;

import lombok.Value;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Collection;
import java.util.List;

@Value
public class GitCommitHistory implements Section<RevCommit> {

    List<RevCommit> commits;

    @Override
    public String getTitle() {
        return "Git Commit History";
    }

    @Override
    public Collection<RevCommit> getItems() {
        return unmodifiableList(commits);
    }
}

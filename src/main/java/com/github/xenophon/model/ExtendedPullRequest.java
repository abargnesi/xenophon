package com.github.xenophon.model;

import org.eclipse.egit.github.core.PullRequest;

public class ExtendedPullRequest extends PullRequest {

    private String mergeCommitSha;

    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }
}

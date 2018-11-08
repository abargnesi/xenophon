package com.github.xenophon.model;

import static java.util.Collections.unmodifiableList;

import lombok.Value;

import java.util.Collection;
import java.util.List;

@Value
public class MergedPullRequests implements Section<ExtendedPullRequest> {

    List<ExtendedPullRequest> pullRequests;

    @Override
    public String getTitle() {
        return "Merged Pull Requests";
    }

    @Override
    public Collection<ExtendedPullRequest> getItems() {
        return unmodifiableList(pullRequests);
    }
}

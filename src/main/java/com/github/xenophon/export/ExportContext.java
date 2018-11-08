package com.github.xenophon.export;

import com.github.xenophon.model.GitCommitHistory;
import com.github.xenophon.model.MergedPullRequests;

import lombok.Builder;
import lombok.Value;
import org.eclipse.egit.github.core.RepositoryId;

@Builder
@Value
public class ExportContext {

    private String title;
    private RepositoryId githubRepositoryId;
    private MergedPullRequests mergedPullRequests;
    private GitCommitHistory gitCommitHistory;
}

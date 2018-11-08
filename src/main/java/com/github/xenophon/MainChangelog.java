package com.github.xenophon;

import com.github.xenophon.data.GitCommits;
import com.github.xenophon.data.PullRequests;
import com.github.xenophon.export.ExportContext;
import com.github.xenophon.model.Project;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class MainChangelog {

    public static void main(String[] args) throws IOException, TemplateException {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token("6fa5ddd33f1419f2a591cfdc84bceb6ec4d1f259");

        final RepositoryId xenophonRepoId = new RepositoryId("abargnesi", "xenophon");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository gitRepository = builder
            .findGitDir(new File("/home/tony/repos/xenophon"))
            .build();

        final String latest   = "RELEASE-145";
        final String earliest = "RELEASE-144";

        final ExportContext context = ExportContext.builder()
            .title("Release145 :: 2018-09-20")
            .githubRepositoryId(xenophonRepoId)
            .mergedPullRequests(
                PullRequests
                    .mergedPullRequestsLastThirtyDays(client, new Project(xenophonRepoId, gitRepository), latest, earliest))
            .gitCommitHistory(
                GitCommits
                    .gitCommitHistory(new Project(xenophonRepoId, gitRepository), latest, earliest))
            .build();

        Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_28);
        freemarkerConfig.setDefaultEncoding(StandardCharsets.UTF_8.name());
        freemarkerConfig.setClassForTemplateLoading(MainChangelog.class, "/");

        final Template changelogTemplate = freemarkerConfig.getTemplate("changelog-template.tpl");
        changelogTemplate.process(context, new PrintWriter(new FileWriter("changelog.md")));
    }
}

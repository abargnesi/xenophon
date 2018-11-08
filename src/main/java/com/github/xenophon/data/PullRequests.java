package com.github.xenophon.data;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;

import com.github.xenophon.model.MergedPullRequests;
import com.github.xenophon.model.Project;
import com.github.xenophon.tools.GitHub;

import lombok.experimental.UtilityClass;
import org.eclipse.egit.github.core.client.GitHubClient;

import java.time.ZonedDateTime;
import java.util.Date;

@UtilityClass
public class PullRequests {

    public MergedPullRequests mergedPullRequestsLastThirtyDays(
        final GitHubClient client, final Project project,
        final String latest, final String earliest) {

        final Date thirtyDaysAgoUTC = Date.from(ZonedDateTime.now(UTC).minusDays(30).toInstant());
        return new MergedPullRequests(
            GitHub
                .mergedPullRequests(
                    client, project,
                    latest, earliest,
                    pr -> pr.getMergedAt().after(thirtyDaysAgoUTC))
                .collect(toList()));
    }
}

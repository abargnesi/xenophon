## ${title}

### Details

<details>
    <summary>${mergedPullRequests.title} (${mergedPullRequests.items?size})</summary>

| # | Title | User | Merged | Merge Commit |
|---|-------|------|--------|--------------|
<#list mergedPullRequests.items as i>
| [${i.number?c}](${i.url}) | ${i.title} | ${i.user.login} | ${i.mergedAt?string('yyyy-MM-dd HH:mm:ss')} | [${i.mergeCommitSha}](https://github.com/abargnesi/xenophon/commit/${i.mergeCommitSha}) |
</#list>

</details>

<details>
    <summary>${gitCommitHistory.title} (${gitCommitHistory.items?size})</summary>

| Title | Commit |
|-------|--------|
<#list gitCommitHistory.items as i>
| ${i.shortMessage} | [${i.name()}](https://github.com/abargnesi/xenophon/commit/${i.name()}) |
</#list>

</details>

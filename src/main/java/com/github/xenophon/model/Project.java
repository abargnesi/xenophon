package com.github.xenophon.model;

import lombok.Value;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.jgit.lib.Repository;

@Value
public class Project {

    RepositoryId githubRepositoryId;
    Repository gitRepository;
}

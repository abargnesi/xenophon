package com.github.xenophon.tools;

import static com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_8;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Java {

    static JavaParser parser = new JavaParser(new ParserConfiguration().setLanguageLevel(JAVA_8));

    public Map<String, String> summarize(@NonNull final ObjectId base, @NonNull final ObjectId head, @NonNull final Repository repository)
        throws GitAPIException {

        try {
            final RevWalk revWalk = new RevWalk(repository);
            final RevTree baseTree = revWalk.parseTree(base);
            final RevTree headTree = revWalk.parseTree(head);

            // prepare the two iterators to compute the diff between
            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, baseTree);
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, headTree);

                try (final org.eclipse.jgit.api.Git git = Git.porcelainAPI(repository)) {
                    List<DiffEntry> diffs = git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .call();
                    for (DiffEntry entry : diffs) {
                        System.out.println("Entry: " + entry);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error summarizing commit.", e);
        }

        return Collections.emptyMap();
    }
}

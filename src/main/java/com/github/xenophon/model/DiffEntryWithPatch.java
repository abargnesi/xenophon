package com.github.xenophon.model;

import lombok.Value;
import org.eclipse.jgit.diff.DiffEntry;

@Value
public class DiffEntryWithPatch {
    private DiffEntry diff;
    private String patch;
}

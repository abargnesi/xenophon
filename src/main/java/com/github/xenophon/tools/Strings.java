package com.github.xenophon.tools;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

@UtilityClass
public class Strings {

    public String basename(@NonNull final String path) {
        return FilenameUtils.getBaseName(path);
    }

    public Stream<String> parsePascalCase(final String text) {
        if (StringUtils.isBlank(text)) {
            return Stream.empty();
        }

        return Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(text));
    }

    public String gitParent(@NonNull final String revisionReference) {
        if (revisionReference.isEmpty()) {
            throw new IllegalArgumentException("revisionReference must be a git revision");
        }
        return revisionReference + "^";
    }
}

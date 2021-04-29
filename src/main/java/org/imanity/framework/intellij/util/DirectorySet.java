package org.imanity.framework.intellij.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@RequiredArgsConstructor
public class DirectorySet {

    private final Path source, resource, testSource, testResource;

    public static DirectorySet create(Path dir) throws IOException {
        Path sourceDirectory = dir.resolve("src/main/java");
        Path resourceDirectory = dir.resolve("src/main/resources");
        Path testSourceDirectory = dir.resolve("src/test/java");
        Path testResourceDirectory = dir.resolve("src/test/resources");
        Files.createDirectories(sourceDirectory);
        Files.createDirectories(resourceDirectory);
        Files.createDirectories(testSourceDirectory);
        Files.createDirectories(testResourceDirectory);
        return new DirectorySet(sourceDirectory, resourceDirectory, testSourceDirectory, testResourceDirectory);
    }

}

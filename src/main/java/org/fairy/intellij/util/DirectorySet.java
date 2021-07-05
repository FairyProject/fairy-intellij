package org.fairy.intellij.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@RequiredArgsConstructor
public class DirectorySet {

    private final Path source, resource, testSource, testResource;

    public static DirectorySet createJava(Path dir) throws IOException {
        Path javaSourceDirectory = dir.resolve("src/main/java");
        Path resourceDirectory = dir.resolve("src/main/resources");
        Path javaTestSourceDirectory = dir.resolve("src/test/java");
        Path testResourceDirectory = dir.resolve("src/test/resources");
        Files.createDirectories(javaSourceDirectory);
        Files.createDirectories(resourceDirectory);
        Files.createDirectories(javaTestSourceDirectory);
        Files.createDirectories(testResourceDirectory);
        return new DirectorySet(javaSourceDirectory, resourceDirectory, javaTestSourceDirectory, testResourceDirectory);
    }

    public static DirectorySet createKotlin(Path dir) throws IOException {
        Path kotlinSourceDirectory = dir.resolve("src/main/kotlin");
        Path resourceDirectory = dir.resolve("src/main/resources");
        Path kotlinTestSourceDirectory = dir.resolve("src/test/kotlin");
        Path testResourceDirectory = dir.resolve("src/test/resources");
        Files.createDirectories(kotlinSourceDirectory);
        Files.createDirectories(resourceDirectory);
        Files.createDirectories(kotlinTestSourceDirectory);
        Files.createDirectories(testResourceDirectory);
        return new DirectorySet(kotlinSourceDirectory, resourceDirectory, kotlinTestSourceDirectory, testResourceDirectory);
    }

}

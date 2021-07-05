package org.fairy.intellij.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Data
public class GradleFiles<T> {

    private final T buildGradle;

    @Nullable
    private final T gradleProperties;
    @Nullable
    private final T settingsGradle;

}

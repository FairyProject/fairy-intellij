package org.fairy.intellij.modules.creator.step;

import com.google.common.base.Charsets;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.modules.template.CommonTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
public class GitIgnoreStep implements CreatorStep {

    private final Project project;
    private final Path directory;

    private final FairyProjectSystem.ProjectType projectType;

    @Override
    public void run(ProgressIndicator indicator) {
        final Path file = this.directory.resolve(".gitignore");

        final String content;
        try {
            switch (this.projectType) {
                case MAVEN:
                    content = CommonTemplate.INSTANCE.applyMavenGitIgnore(this.project);
                    break;
                case KOTLIN_GRADLE:
                case GRADLE:
                    content = CommonTemplate.INSTANCE.applyGradleGitIgnore(this.project);
                    break;
                default:
                    throw new IllegalStateException("Unknown Project Type " + this.projectType.name());
            }
            Files.write(file, content.getBytes(Charsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalArgumentException("An error occurs while writing .gitignore file", e);
        }
    }

}

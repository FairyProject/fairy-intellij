package org.imanity.framework.intellij.modules.creator.step;

import com.google.common.base.Charsets;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.modules.exception.ModuleBuilderException;
import org.imanity.framework.intellij.util.ApplicationUtil;
import org.imanity.framework.intellij.util.GradleFiles;
import org.imanity.framework.intellij.util.PathUtil;
import org.imanity.framework.intellij.util.PsiUtil;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BasicGradleSetupStep implements CreatorStep {

    private final Project project;
    private final Path directory;
    private final FrameworkProjectSystem projectSystem;
    private final GradleFiles<String> gradleFiles;

    @Override
    public void run(ProgressIndicator indicator) {
        GradleFiles<Path> pathGradleFiles;
        try {
            pathGradleFiles = this.setupGradlePath(this.gradleFiles, this.directory);
        } catch (IOException e) {
            throw new IllegalStateException("An error occurs while setting up gradle paths", e);
        }

        ApplicationUtil.runWriteTask(() -> {
            PsiFile buildGradlePsi;
            try {
                buildGradlePsi = addBuildGradleDependencies(this.gradleFiles.getBuildGradle());
            } catch (Throwable throwable) {
                throw new ModuleBuilderException("An error occurs while adding dependencies for build.gradle", throwable);
            }

            final PsiManager psiManager = PsiManager.getInstance(project);
            final PsiDirectory directory = psiManager.findDirectory(PathUtil.findVirtualFileOrError(this.directory));
            if (directory != null) {
                final PsiFile oldFile = directory.findFile(buildGradlePsi.getName());
                if (oldFile != null) {
                    oldFile.delete();
                }
                final PsiElement element = directory.add(buildGradlePsi);
                if (element instanceof PsiFile) {
                    new ReformatCodeProcessor((PsiFile) element, false).run();
                }
            }

            if (pathGradleFiles.getGradleProperties() != null && gradleFiles.getGradleProperties() != null) {
                try {
                    this.writeText(pathGradleFiles.getGradleProperties(), this.gradleFiles.getGradleProperties(), psiManager);
                } catch (IOException e) {
                    throw new IllegalStateException("An error occurs while writing gradle.properties", e);
                }
            }

            if (pathGradleFiles.getSettingsGradle() != null && gradleFiles.getSettingsGradle() != null) {
                try {
                    this.writeText(pathGradleFiles.getSettingsGradle(), this.gradleFiles.getSettingsGradle(), psiManager);
                } catch (IOException e) {
                    throw new IllegalStateException("An error occurs while writing settings.gradle", e);
                }
            }
        });
    }

    private void writeText(Path file, String text, PsiManager psiManager) throws IOException {
        Files.write(file, text.getBytes(Charsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        if (psiManager != null) {
            final PsiFile psiFile = psiManager.findFile(PathUtil.findVirtualFileOrError(file));
            if (psiFile != null) {
                new ReformatCodeProcessor(psiFile, false).run();
            }
        }
    }

    private PsiFile addBuildGradleDependencies(String buildGradlePath) throws Throwable {
        final PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText(GroovyLanguage.INSTANCE, buildGradlePath);

        PsiUtil.runWriteAction(file, () -> {
            final String fileName = "build.gradle";
            file.setName(fileName);

            final GroovyFile groovyFile = (GroovyFile) file;
            this.addRepositoryOrDependencies(groovyFile, "repositories", this.projectSystem.getBuildRepositories()
                    .stream()
                    // TODO - check repository project type?
                    .map(repository -> "maven { url = '" + repository.getUrl() + "' }")
                    .collect(Collectors.toList())
            );

            this.addRepositoryOrDependencies(groovyFile, "dependencies", this.projectSystem.getBuildDependencies()
                .stream()
                    .filter(dependency -> dependency.getTypes().contains(FrameworkProjectSystem.ProjectType.GRADLE) ||
                                          dependency.getTypes().contains(FrameworkProjectSystem.ProjectType.KOTLIN_GRADLE))
                    .map(dependency -> String.format("%s '%s:%s:%s'", dependency.getGradleConfiguration(), dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()))
                    .collect(Collectors.toList())
            );
        });
        return file;
    }

    private void addRepositoryOrDependencies(GroovyFile groovyFile, String name, List<String> expressions) {
        final GrClosableBlock block = this.getClosableBlockByName(groovyFile, name);
        if (block == null) {
            throw new IllegalStateException("Failed to parse build.gradle files");
        }

        final String expressionText = String.join("\n", expressions);

        final GroovyFile fakeFile = GroovyPsiElementFactory.getInstance(project).createGroovyFile(expressionText, false, null);
        final PsiElement last = block.getChildren()[block.getChildren().length - 1];
        block.addBefore(fakeFile, last);
    }

    private GrClosableBlock getClosableBlockByName(PsiElement element, String name) {
        return Arrays.stream(element.getChildren())
                .filter(it -> Arrays.stream(it.getChildren()).anyMatch(child -> child instanceof GrReferenceExpression && child.getText().equals(name)))
                .map(it -> Arrays.stream(it.getChildren())
                        .filter(Objects::nonNull)
                        .filter(child -> child instanceof GrClosableBlock)
                        .map(child -> (GrClosableBlock) child).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private GradleFiles<Path> setupGradlePath(GradleFiles<String> files, Path directory) throws IOException {
        final GradleFiles<Path> pathGradleFiles = new GradleFiles<>(
                directory.resolve("build.gradle"),
                files.getGradleProperties() != null ? directory.resolve("gradle.properties") : null,
                files.getSettingsGradle() != null ? directory.resolve("settings.gradle") : null
        );
        Files.createFile(pathGradleFiles.getBuildGradle());
        if (pathGradleFiles.getGradleProperties() != null) {
            Files.createFile(pathGradleFiles.getGradleProperties());
        }
        if (pathGradleFiles.getSettingsGradle() != null) {
            Files.createFile(pathGradleFiles.getSettingsGradle());
        }
        return pathGradleFiles;
    }
}

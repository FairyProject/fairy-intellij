package org.imanity.framework.intellij.modules.creator.step;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.util.GradleFiles;
import org.imanity.framework.intellij.util.PsiUtil;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GroovyGradleSetupStep extends AbstractGradleSetupStep {

    public GroovyGradleSetupStep(Project project, Path directory, FrameworkProjectSystem projectSystem, GradleFiles<String> gradleFiles) {
        super(project, directory, projectSystem, gradleFiles);
    }

    protected PsiFile addBuildGradleDependencies(String buildGradlePath) throws Throwable {
        final PsiFile file = PsiFileFactory.getInstance(this.getProject()).createFileFromText(GroovyLanguage.INSTANCE, buildGradlePath);

        PsiUtil.runWriteAction(file, () -> {
            final String fileName = "build.gradle";
            file.setName(fileName);

            final GroovyFile groovyFile = (GroovyFile) file;
            this.addRepositoryOrDependencies(groovyFile, "repositories", this.getProjectSystem().getBuildRepositories()
                    .stream()
                    .filter(repository -> repository.getTypes().contains(FrameworkProjectSystem.ProjectType.GRADLE))
                    .map(repository -> repository.toGradleString(false))
                    .collect(Collectors.toList())
            );

            this.addRepositoryOrDependencies(groovyFile, "dependencies", this.getProjectSystem().getBuildDependencies()
                .stream()
                    .filter(dependency -> dependency.getTypes().contains(FrameworkProjectSystem.ProjectType.GRADLE))
                    .map(dependency -> String.format("%s '%s:%s:%s'", dependency.getGradleScope(), dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()))
                    .collect(Collectors.toList())
            );
        });
        return file;
    }

    protected void addRepositoryOrDependencies(GroovyFile groovyFile, String name, List<String> expressions) {
        final GrClosableBlock block = this.getClosableBlockByName(groovyFile, name);
        if (block == null) {
            throw new IllegalStateException("Failed to parse build.gradle files");
        }

        final String expressionText = String.join("\n", expressions);

        final GroovyFile fakeFile = GroovyPsiElementFactory.getInstance(this.getProject()).createGroovyFile(expressionText, false, null);
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

    protected GradleFiles<Path> setupGradlePath(GradleFiles<String> files, Path directory) throws IOException {
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

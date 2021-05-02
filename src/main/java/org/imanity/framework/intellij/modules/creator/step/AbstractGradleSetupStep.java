package org.imanity.framework.intellij.modules.creator.step;

import com.google.common.base.Charsets;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.modules.exception.ModuleBuilderException;
import org.imanity.framework.intellij.util.ApplicationUtil;
import org.imanity.framework.intellij.util.GradleFiles;
import org.imanity.framework.intellij.util.PathUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
@Getter(AccessLevel.PACKAGE)
public abstract class AbstractGradleSetupStep implements CreatorStep {

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
                buildGradlePsi = this.addBuildGradleDependencies(gradleFiles.getBuildGradle());
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

    protected abstract PsiFile addBuildGradleDependencies(String buildGradle) throws Throwable;

    protected abstract GradleFiles<Path> setupGradlePath(GradleFiles<String> gradleFiles, Path directory) throws IOException;
}

package org.fairy.intellij.modules.creator.step;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.util.GradleFiles;
import org.fairy.intellij.util.PsiUtil;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class KotlinGradleSetupStep extends AbstractGradleSetupStep {

    public KotlinGradleSetupStep(Project project, Path directory, FairyProjectSystem projectSystem, GradleFiles<String> gradleFiles) {
        super(project, directory, projectSystem, gradleFiles);
    }

    public PsiFile addBuildGradleDependencies(String buildGradlePath) throws Throwable {
        final PsiFile file = PsiFileFactory.getInstance(this.getProject()).createFileFromText(KotlinLanguage.INSTANCE, buildGradlePath);

        PsiUtil.runWriteAction(file, () -> {
            final String fileName = "build.gradle.kts";
            file.setName(fileName);

            final KtFile groovyFile = (KtFile) file;
            this.addRepositoryOrDependencies(groovyFile, "repositories", this.getProjectSystem().getBuildRepositories()
                    .stream()
                    .filter(repository -> repository.getTypes().contains(FairyProjectSystem.ProjectType.KOTLIN_GRADLE))
                    .map(repository -> repository.toGradleString(true))
                    .collect(Collectors.toList())
            );

            this.addRepositoryOrDependencies(groovyFile, "dependencies", this.getProjectSystem().getBuildDependencies()
                .stream()
                    .filter(dependency -> dependency.getTypes().contains(FairyProjectSystem.ProjectType.KOTLIN_GRADLE))
                    .map(dependency -> String.format("%s(\"%s:%s:%s\")", dependency.getGradleScope(), dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()))
                    .collect(Collectors.toList())
            );
        });
        return file;
    }

    private void addRepositoryOrDependencies(KtFile groovyFile, String name, List<String> expressions) {
        final KtLambdaArgument block = this.getClosableBlockByName(groovyFile, name);
        if (block == null) {
            throw new IllegalStateException("Failed to parse build.gradle files");
        }

        final String expressionText = String.join("\n", expressions);

        final KtFile fakeFile = (KtFile) PsiFileFactory.getInstance(this.getProject()).createFileFromText(KotlinLanguage.INSTANCE, expressionText);
        final KtLambdaExpression lambdaExpression = block.getLambdaExpression();
        if (lambdaExpression == null) {
            throw new IllegalStateException("Failed to parse build.gradle files");
        }

        final KtBlockExpression expression = lambdaExpression.getBodyExpression();
        if (expression != null) {
            if (expression.getChildren().length > 0) {
                final PsiElement last = expression.getChildren()[expression.getChildren().length - 1];
                expression.addBefore(fakeFile, last);
            } else {
                expression.add(fakeFile);
            }
        }
    }

    private KtLambdaArgument getClosableBlockByName(PsiElement element, String name) {
        final KtScript script = PsiUtil.getPsiChildrenByType(element, KtScript.class);
        if (script == null) {
            return null;
        }

        final KtBlockExpression blockExpression = script.getBlockExpression();

        for (PsiElement childElement : blockExpression.getChildren()) {
            if (childElement instanceof KtScriptInitializer) {
                for (PsiElement scriptChild : childElement.getChildren()) {
                    if (scriptChild instanceof KtCallExpression) {
                        final KtCallExpression callExpression = (KtCallExpression) scriptChild;

                        String expressionName = null;

                        for (PsiElement callExpressionChild : callExpression.getChildren()) {
                            if (callExpressionChild instanceof KtNameReferenceExpression) {
                                if (expressionName != null) {
                                    continue;
                                }
                                expressionName = callExpressionChild.getText();
                            }
                            if (callExpressionChild instanceof KtLambdaArgument) {
                                final KtLambdaArgument lambdaArgument = (KtLambdaArgument) callExpressionChild;

                                if (expressionName == null) {
                                    throw new IllegalStateException("Expression Name is null.");
                                }

                                if (expressionName.equals(name)) {
                                    return lambdaArgument;
                                }
                            }
                        }

                        break;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected GradleFiles<Path> setupGradlePath(GradleFiles<String> files, Path directory) throws IOException {
        final GradleFiles<Path> pathGradleFiles = new GradleFiles<>(
                directory.resolve("build.gradle.kts"),
                files.getGradleProperties() != null ? directory.resolve("gradle.properties") : null,
                files.getSettingsGradle() != null ? directory.resolve("settings.gradle.kts") : null
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

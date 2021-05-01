package org.imanity.framework.intellij.modules.creator.step;

import com.google.common.base.Charsets;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.ImanityFrameworkIntelliJ;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.util.GradleUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

@RequiredArgsConstructor
public class GradleWrapperStep implements CreatorStep {

    private final Project project;
    private final Path directory;
    private final FrameworkProjectSystem projectSystem;

    @Override
    public void run(ProgressIndicator indicator) {
        final String wrapperVersion = ImanityFrameworkIntelliJ.DEFAULT_WRAPPER_VERSION; // TODO

        final Path wrapperDir = this.directory.resolve("gradle/wrapper");
        try {
            Files.createDirectories(wrapperDir);
        } catch (IOException e) {
            throw new IllegalStateException("An error occurs while creating directory for gradle/wrapper", e);
        }

        final Path wrapperProp = wrapperDir.resolve("gradle-wrapper.properties");
        String text = "distributionUrl=https\\://services.gradle.org/distributions/gradle-" + wrapperVersion + "-bin.zip\n";

        try {
            Files.write(wrapperProp, text.getBytes(Charsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("An error occurs while writing gradle wrapper properties", e);
        }

        indicator.setText("Setting up gradle wrapper");
        indicator.setText2("Running Gradle task: 'wrapper'");
        GradleUtil.runGradleTaskAndWait(project, directory, settings -> settings.setTaskNames(Collections.singletonList("wrapper")));

        indicator.setText2(null);
    }
}

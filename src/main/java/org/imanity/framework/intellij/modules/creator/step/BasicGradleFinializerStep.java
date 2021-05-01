package org.imanity.framework.intellij.modules.creator.step;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.util.ApplicationUtil;
import org.imanity.framework.intellij.util.GradleUtil;
import org.imanity.framework.intellij.util.PathUtil;
import org.jetbrains.plugins.gradle.service.execution.GradleExternalTaskConfigurationType;
import org.jetbrains.plugins.gradle.service.project.open.GradleProjectImportUtil;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import java.nio.file.Path;
import java.util.Collections;

@RequiredArgsConstructor
public class BasicGradleFinializerStep implements CreatorStep {

    private final Module module;
    private final Path directory;
    private final FrameworkProjectSystem projectSystem;

    @Override
    public void run(ProgressIndicator indicator) {
        PathUtil.findVirtualFileOrError(this.directory).refresh(false, true);
        final Project project = this.module.getProject();

        ApplicationUtil.invokeLater(() -> {
            GradleProjectImportUtil.linkAndRefreshGradleProject(this.directory.toAbsolutePath().toString(), project);
            GradleUtil.showProgress(project);
        });

        final GradleExternalTaskConfigurationType gradleType = GradleExternalTaskConfigurationType.getInstance();

        final RunManager runManager = RunManager.getInstance(project);
        final String runConfigName = this.projectSystem.getArtifactId() + " build";

        final ExternalSystemRunConfiguration runConfiguration = new ExternalSystemRunConfiguration(
                GradleConstants.SYSTEM_ID,
                project,
                gradleType.getConfigurationFactories()[0],
                runConfigName
        );

        runConfiguration.getSettings().setExternalProjectPath(directory.toAbsolutePath().toString());
        runConfiguration.getSettings().setExecutionName(runConfigName);
        runConfiguration.getSettings().setTaskNames(Collections.singletonList("build"));

        runConfiguration.setAllowRunningInParallel(false);

        final RunnerAndConfigurationSettings settings = runManager.createConfiguration(
                runConfiguration,
                GradleExternalTaskConfigurationType.getInstance().getConfigurationFactories()[0]
        );

        settings.setActivateToolWindowBeforeRun(true);
        settings.storeInLocalWorkspace();

        runManager.addConfiguration(settings);
        if (runManager.getSelectedConfiguration() == null) {
            runManager.setSelectedConfiguration(settings);
        }
    }
}

package org.imanity.framework.intellij.modules.creator.step;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.util.ApplicationUtil;
import org.imanity.framework.intellij.util.PathUtil;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
public class BasicMavenFinalizerStep implements CreatorStep {

    private final Module module;
    private final Path directory;

    @Override
    public void run(ProgressIndicator indicator) {
        final Project project = this.module.getProject();
        if (this.module.isDisposed() || project.isDisposed()) {
            return;
        }

        final Path pomXml = this.directory.resolve("pom.xml");
        final VirtualFile vPomXml = PathUtil.findVirtualFileOrError(pomXml);

        ApplicationUtil.invokeLater(() -> {
            final MavenProjectsManager manager = MavenProjectsManager.getInstance(project);

            manager.addManagedFilesOrUnignore(Collections.singletonList(vPomXml));
            manager.getImportingSettings().setDownloadDocsAutomatically(true);
            manager.getImportingSettings().setDownloadSourcesAutomatically(true);

            // Setup the default Maven run config
            final MavenRunnerParameters mavenRunnerParameters = new MavenRunnerParameters();
            mavenRunnerParameters.setWorkingDirPath(this.directory.toAbsolutePath().toString());
            mavenRunnerParameters.setGoals(Arrays.asList("clean", "package"));
            final RunnerAndConfigurationSettings runnerSettings = MavenRunConfigurationType.createRunnerAndConfigurationSettings(null, null, mavenRunnerParameters, project);
            runnerSettings.setName(this.module.getName() + " build");
            runnerSettings.storeInLocalWorkspace();

            final RunManager runManager = RunManager.getInstance(project);
            runManager.addConfiguration(runnerSettings);
            if (runManager.getSelectedConfiguration() == null) {
                runManager.setSelectedConfiguration(runnerSettings);
            }
        });
    }
}

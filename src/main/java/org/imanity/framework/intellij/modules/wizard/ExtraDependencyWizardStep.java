package org.imanity.framework.intellij.modules.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.ImanityFrameworkIntelliJ;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;

import javax.swing.*;

@RequiredArgsConstructor
public class ExtraDependencyWizardStep extends ModuleWizardStep {
    private JPanel panel;
    private JCheckBox lombokCheckBox;
    private JCheckBox discordCheckBox;
    private JCheckBox httpCheckBox;

    private final FrameworkProjectSystem projectSystem;

    @Override
    public JComponent getComponent() {
        return this.panel;
    }

    @Override
    public void updateDataModel() {
        if (this.lombokCheckBox.isSelected()) {
            this.projectSystem.getBuildDependencies().add(new FrameworkProjectSystem.BuildDependency(
                    "org.projectlombok",
                    "lombok",
                    "1.18.20",
                    "provided",
                    "compileOnly"
            ));

            // Annotation Processor for Gradle
            this.projectSystem.getBuildDependencies().add(new FrameworkProjectSystem.BuildDependency(
                    "org.projectlombok",
                    "lombok",
                    "1.18.20",
                    "none",
                    "annotationProcessor",
                    FrameworkProjectSystem.ProjectType.GRADLE, FrameworkProjectSystem.ProjectType.KOTLIN_GRADLE
            ));
        }
        if (this.discordCheckBox.isSelected()) {
            this.projectSystem.getBuildDependencies().add(new FrameworkProjectSystem.BuildDependency(
                    "org.imanity.framework",
                    "discord",
                    ImanityFrameworkIntelliJ.getLatestFrameworkVersion(),
                    "compile",
                    "implementation"
            ));
        }
        if (this.httpCheckBox.isSelected()) {
            this.projectSystem.getBuildDependencies().add(new FrameworkProjectSystem.BuildDependency(
                    "org.imanity.framework",
                    "http-server",
                    ImanityFrameworkIntelliJ.getLatestFrameworkVersion(),
                    "compile",
                    "implementation"
            ));
        }
    }
}

package org.fairy.intellij.modules.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import lombok.RequiredArgsConstructor;
import org.fairy.intellij.FairyIntelliJ;
import org.fairy.intellij.modules.FairyProjectSystem;

import javax.swing.*;

@RequiredArgsConstructor
public class ExtraDependencyWizardStep extends ModuleWizardStep {
    private JPanel panel;
    private JCheckBox lombokCheckBox;
    private JCheckBox discordCheckBox;
    private JCheckBox httpCheckBox;

    private final FairyProjectSystem projectSystem;

    @Override
    public JComponent getComponent() {
        return this.panel;
    }

    @Override
    public void updateDataModel() {
        if (this.lombokCheckBox.isSelected()) {
            this.projectSystem.getBuildDependencies().add(new FairyProjectSystem.BuildDependency(
                    "org.projectlombok",
                    "lombok",
                    "1.18.20",
                    "provided",
                    "compileOnly"
            ));

            // Annotation Processor for Gradle
            this.projectSystem.getBuildDependencies().add(new FairyProjectSystem.BuildDependency(
                    "org.projectlombok",
                    "lombok",
                    "1.18.20",
                    "none",
                    "annotationProcessor",
                    "kapt",
                    FairyProjectSystem.ProjectType.GRADLE, FairyProjectSystem.ProjectType.KOTLIN_GRADLE
            ));
        }
        if (this.discordCheckBox.isSelected()) {
            this.projectSystem.getBuildDependencies().add(new FairyProjectSystem.BuildDependency(
                    "org.fairy",
                    "discord",
                    FairyIntelliJ.getLatestFrameworkVersion(),
                    "compile",
                    "implementation"
            ));
        }
        if (this.httpCheckBox.isSelected()) {
            this.projectSystem.getBuildDependencies().add(new FairyProjectSystem.BuildDependency(
                    "org.fairy",
                    "http-server",
                    FairyIntelliJ.getLatestFrameworkVersion(),
                    "compile",
                    "implementation"
            ));
        }
    }
}

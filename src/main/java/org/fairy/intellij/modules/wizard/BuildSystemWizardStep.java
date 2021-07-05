package org.fairy.intellij.modules.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.modules.exception.EmptyFieldModuleBuilderException;
import org.fairy.intellij.util.StringUtil;
import org.fairy.intellij.modules.exception.FieldModuleBuilderException;

import javax.swing.*;

public class BuildSystemWizardStep extends ModuleWizardStep {
    private JTextField groupIdField;
    private JTextField artifactIdField;
    private JTextField versionField;
    private JPanel panel;
    private JComboBox<String> projectTypeBox;

    private final FairyProjectSystem projectCreator;

    public BuildSystemWizardStep(FairyProjectSystem projectCreator) {
        this.projectCreator = projectCreator;
    }

    @Override
    public JComponent getComponent() {
        return this.panel;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        try {
            if (this.groupIdField.getText().isEmpty()) {
                throw new EmptyFieldModuleBuilderException(this.groupIdField);
            }

            if (this.artifactIdField.getText().isEmpty()) {
                throw new EmptyFieldModuleBuilderException(this.artifactIdField);
            }

            if (this.versionField.getText().isEmpty()) {
                throw new EmptyFieldModuleBuilderException(this.versionField);
            }

            if (StringUtil.isAnySpace(this.groupIdField.getText())) {
                throw new FieldModuleBuilderException(this.groupIdField, "The Group ID Field must not contain any space!");
            }

            if (StringUtil.isAnySpace(this.groupIdField.getText())) {
                throw new FieldModuleBuilderException(this.artifactIdField, "The Artifact ID Field must not contain any space!");
            }

            if (this.projectTypeBox.getSelectedItem() == null) {
                throw new EmptyFieldModuleBuilderException(this.projectTypeBox);
            }
        } catch (FieldModuleBuilderException ex) {
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(ex.getMessage(), MessageType.ERROR, null)
                    .setFadeoutTime(2500)
                    .createBalloon()
                    .show(RelativePoint.getSouthOf(ex.getField()), Balloon.Position.below);
            return false;
        }

        return true;
    }

    @Override
    public void updateDataModel() {
        this.projectCreator.setGroupId(this.groupIdField.getText());
        this.projectCreator.setArtifactId(this.artifactIdField.getText());
        this.projectCreator.setVersion(this.versionField.getText());

        switch (this.projectTypeBox.getSelectedItem().toString()) {
            case "Maven":
                this.projectCreator.setProjectType(FairyProjectSystem.ProjectType.MAVEN);
                break;
            case "Gradle":
                this.projectCreator.setProjectType(FairyProjectSystem.ProjectType.GRADLE);
                break;
            case "Kotlin-Gradle":
                this.projectCreator.setProjectType(FairyProjectSystem.ProjectType.KOTLIN_GRADLE);
                break;
        }
    }

}

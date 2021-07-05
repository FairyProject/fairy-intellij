package org.fairy.intellij.modules.wizard;

import com.google.common.collect.Lists;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import lombok.RequiredArgsConstructor;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.modules.exception.EmptyFieldModuleBuilderException;
import org.fairy.intellij.util.StringUtil;
import org.fairy.intellij.modules.exception.FieldModuleBuilderException;

import javax.swing.*;
import java.util.List;

@RequiredArgsConstructor
public class BukkitWizardStep extends ModuleWizardStep {
    private JPanel panel;
    private JTextField pluginNameField;
    private JTextField mainClassField;
    private JTextField descriptionField;
    private JTextField authorsField;
    private JTextField dependenciesField;
    private JTextField softDependenciesField;
    private JComboBox<String> loadOrderField;
    private JTextField loadBeforeField;

    private final FairyProjectSystem projectSystem;

    @Override
    public JComponent getComponent() {
        this.pluginNameField.setText(this.projectSystem.getArtifactId());
        this.mainClassField.setText(this.projectSystem.getGroupId() + "." + this.projectSystem.getArtifactId());

        return this.panel;
    }

    @Override
    public boolean isStepVisible() {
        return this.projectSystem.getPlatforms().contains(FairyProjectSystem.Platform.SPIGOT);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        try {
            if (this.pluginNameField.getText().isEmpty()) {
                throw new EmptyFieldModuleBuilderException(this.pluginNameField);
            }

            final String mainClass = this.mainClassField.getText();
            if (mainClass.isEmpty()) {
                throw new EmptyFieldModuleBuilderException(this.mainClassField);
            }

            if (StringUtil.isAnySpace(mainClass)) {
                throw new FieldModuleBuilderException(mainClassField, "The Main Class Field must not contain any space!");
            }

            if (mainClass.endsWith(".")) {
                throw new FieldModuleBuilderException(mainClassField, "The Main Class Field must not end with a dot!");
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
        this.projectSystem.setName(this.pluginNameField.getText());
        this.projectSystem.setMainClass(this.mainClassField.getText());

        this.projectSystem.setDescription(this.descriptionField.getText());
        this.projectSystem.setAuthors(this.findStringArrayByText(this.authorsField.getText()));
        this.projectSystem.setDependencies(this.findStringArrayByText(this.dependenciesField.getText()));
        this.projectSystem.setSoftDependencies(this.findStringArrayByText(this.softDependenciesField.getText()));
        this.projectSystem.setLoadBefore(this.findStringArrayByText(this.loadBeforeField.getText()));
        this.projectSystem.setLoadOrder((String) this.loadOrderField.getSelectedItem());
    }

    private String[] findStringArrayByText(String text) {
        if (text.isEmpty()) {
            return new String[0];
        }

        String[] all;

        if (text.contains(", ")) {
            all = text.split(", ");
        } else if (text.contains(",")) {
            all = text.split(",");
        } else if (text.contains(" ")) {
            all = text.split(" ");
        } else {
            all = new String[] {text};
        }

        List<String> list = Lists.newArrayList(all);
        list.removeIf(String::isEmpty);

        return list.toArray(new String[0]);
    }
}

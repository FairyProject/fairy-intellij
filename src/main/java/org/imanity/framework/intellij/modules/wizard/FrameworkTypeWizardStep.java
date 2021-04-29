package org.imanity.framework.intellij.modules.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class FrameworkTypeWizardStep extends ModuleWizardStep {
    private JPanel panel;
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JCheckBox spigotPlatform;
    private JCheckBox bungeePlatform;

    private final FrameworkProjectSystem projectCreator;

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        final Collection<FrameworkProjectSystem.Platform> platformList = this.createPlatformList();

        if (platformList.size() == 0) {
            String message = "The Project Configuration is incomplete. Please select at least 1 platform to continue";
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(message, null, JBColor.ORANGE, null)
                    .setHideOnAction(true)
                    .setHideOnClickOutside(true)
                    .setHideOnKeyOutside(true)
                    .createBalloon()
            .show(RelativePoint.getSouthOf(panel), Balloon.Position.atRight);
            return false;
        }

        return true;
    }

    @Override
    public void updateDataModel() {
        this.projectCreator.getPlatforms().clear();
        this.projectCreator.getPlatforms().addAll(this.createPlatformList());
    }

    private Collection<FrameworkProjectSystem.Platform> createPlatformList() {
        Set<FrameworkProjectSystem.Platform> platforms = new HashSet<>();

        if (this.spigotPlatform.isSelected()) {
            platforms.add(FrameworkProjectSystem.Platform.SPIGOT);
        }

        if (this.bungeePlatform.isSelected()) {
            platforms.add(FrameworkProjectSystem.Platform.BUNGEE);
        }

        return platforms;
    }
}

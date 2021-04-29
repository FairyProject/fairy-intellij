package org.imanity.framework.intellij.modules;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import org.imanity.framework.intellij.ImanityFrameworkAssets;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FrameworkModuleType extends JavaModuleType {

    public static final String ID = "IMANITY_MODULE_TYPE";

    public static FrameworkModuleType getInstance() {
        return (FrameworkModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @Override
    public @NotNull JavaModuleBuilder createModuleBuilder() {
        return new FrameworkModuleBuilder();
    }

    @Override
    public @NotNull String getName() {
        return "Imanity Framework";
    }

    @Override
    public @NotNull Icon getIcon() {
        return ImanityFrameworkAssets.FRAMEWORK_LOGO;
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return ImanityFrameworkAssets.FRAMEWORK_LOGO;
    }
}

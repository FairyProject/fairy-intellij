package org.fairy.intellij.modules;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import org.fairy.intellij.FairyAssets;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FairyModuleType extends JavaModuleType {

    public static final String ID = "IMANITY_MODULE_TYPE";

    public static FairyModuleType getInstance() {
        return (FairyModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @Override
    public @NotNull JavaModuleBuilder createModuleBuilder() {
        return new FairyModuleBuilder();
    }

    @Override
    public @NotNull String getName() {
        return "Fairy";
    }

    @Override
    public @NotNull Icon getIcon() {
        return FairyAssets.FRAMEWORK_LOGO;
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return FairyAssets.FRAMEWORK_LOGO;
    }
}

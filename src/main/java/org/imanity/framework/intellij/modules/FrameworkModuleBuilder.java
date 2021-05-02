package org.imanity.framework.intellij.modules;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.tuple.Pair;
import org.imanity.framework.intellij.ImanityFrameworkAssets;
import org.imanity.framework.intellij.modules.wizard.BuildSystemWizardStep;
import org.imanity.framework.intellij.modules.wizard.BukkitWizardStep;
import org.imanity.framework.intellij.modules.wizard.ExtraDependencyWizardStep;
import org.imanity.framework.intellij.modules.wizard.FrameworkTypeWizardStep;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FrameworkModuleBuilder extends JavaModuleBuilder {

    private final FrameworkProjectSystem projectCreator = new FrameworkProjectSystem();

    @Override
    public ModuleType<?> getModuleType() {
        return JavaModuleType.getModuleType();
    }

    @Override
    public @Nullable String getBuilderId() {
        return "IMANITY_MODULE";
    }

    @Override
    public int getWeight() {
        return JavaModuleBuilder.BUILD_SYSTEM_WEIGHT - 1;
    }

    @Override
    public String getGroupName() {
        return "Imanity Framework";
    }

    @Override
    public String getPresentableName() {
        return "Imanity Framework";
    }

    @Override
    public String getParentGroup() {
        return FrameworkModuleType.ID;
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        final Project project = modifiableRootModel.getProject();
        final Pair<Path, VirtualFile> pair = this.createAndGetRoot();
        final Path path = pair.getKey();
        final VirtualFile virtualFile = pair.getRight();

        modifiableRootModel.addContentEntry(virtualFile);

        final Sdk moduleJdk = this.getModuleJdk();
        if (moduleJdk != null) {
            modifiableRootModel.setSdk(moduleJdk);
        } else {
            modifiableRootModel.inheritSdk();
        }

        final DumbAwareRunnable runnable = () -> this.projectCreator.create(path, modifiableRootModel.getModule());

        if (project.isDisposed()) {
            return;
        }

        if (ApplicationManager.getApplication().isUnitTestMode() || ApplicationManager.getApplication().isHeadlessEnvironment()) {
            runnable.run();
            return;
        }

        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(runnable);
            return;
        }

        DumbService.getInstance(project).runWhenSmart(runnable);
    }

    private Pair<Path, VirtualFile> createAndGetRoot() {
        final String contentEntryPath = getContentEntryPath();
        if (contentEntryPath == null) {
            throw new IllegalStateException("Failed to get content entry path");
        }

        final String pathName = FileUtil.toSystemIndependentName(contentEntryPath);
        final Path path = Paths.get(pathName);
        try {
            Files.createDirectories(path);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(pathName);
        return Pair.of(path, virtualFile);
    }

    @Override
    public Icon getNodeIcon() {
        return ImanityFrameworkAssets.FRAMEWORK_LOGO;
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[] {
            new BuildSystemWizardStep(this.projectCreator),
            new BukkitWizardStep(this.projectCreator),
            new ExtraDependencyWizardStep(this.projectCreator)
        };
    }

    @Override
    public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new FrameworkTypeWizardStep(projectCreator);
    }
}

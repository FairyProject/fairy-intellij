package org.imanity.framework.intellij.modules;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VfsUtil;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import org.imanity.framework.intellij.modules.creator.BukkitProjectCreator;
import org.imanity.framework.intellij.modules.creator.PendingReformatFiles;
import org.imanity.framework.intellij.modules.creator.ProjectCreator;
import org.imanity.framework.intellij.modules.creator.step.CreatorStep;
import org.imanity.framework.intellij.modules.exception.ModuleBuilderException;
import org.imanity.framework.intellij.util.ApplicationUtil;
import org.imanity.framework.intellij.util.DirectorySet;
import org.imanity.framework.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class FrameworkProjectSystem {

    private final Set<Platform> platforms = new HashSet<>();
    private final List<BuildRepository> repositories = new ArrayList<>();
    private final List<BuildDependency> dependencies = new ArrayList<>();

    private String groupId;
    private String artifactId;
    private String version;
    private ProjectType projectType;

    private String mainClass; // TODO

    private DirectorySet directory; // TODO

    public DirectorySet getDirectoryOrError() {
        if (this.directory == null) {
            throw new IllegalStateException("Project structure is not yet created");
        }

        return this.directory;
    }

    public void create(Path path, Module module) {
        ProgressManager.getInstance().run(new Task.Backgroundable(module.getProject(), "Setting Up project", false) {

            @Override
            public boolean shouldStartInBackground() {
                return false;
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                if (module.isDisposed() || this.getProject().isDisposed()) {
                    return;
                }

                List<WorkLogStep> workLog = new ArrayList<>();

                try {

                    ApplicationUtil.invokeAndWait(() -> VfsUtil.markDirtyAndRefresh(false, true, true, PathUtil.findVirtualFileOrError(path)));

                    if (platforms.size() == 1) {
                        // Single Module

                        Platform platform = platforms.stream().findFirst().get();
                        final WorkLogStep log = newLog(platform, workLog);

                        if (!runSingle(path, module, indicator, log)) {
                            return;
                        }

                        // performCreationSettingSetup ?
                        PendingReformatFiles.reformatAll();
                    } else {
                        // Multi Module
                    }

                    ApplicationUtil.invokeLater(() -> {
                        VfsUtil.markDirtyAndRefresh(false, true, true, PathUtil.findVirtualFileOrError(path));
                    });

                } catch (Exception ex) {
                    StringBuilder workLogText = new StringBuilder("Build steps completed:");

                    for (WorkLogStep step : workLog) {
                        step.printStep(workLogText);
                    }
                    throw new ModuleBuilderException(workLogText.toString(), ex);
                } finally {
                    WorkLogStep.currentStep = null;
                }
            }
        });
    }

    private boolean runSingle(Path path, Module module, ProgressIndicator indicator, WorkLogStep logStep) {
        final ProjectCreator projectCreator = this.buildCreator(path, module);

        final List<CreatorStep> creatorSteps = projectCreator.singleModule();

        for (CreatorStep creatorStep : creatorSteps) {
            if (module.isDisposed() || module.getProject().isDisposed()) {
                return false;
            }
            logStep.newCurrentStep(creatorStep);
            creatorStep.run(indicator);
        }
        logStep.finishCurrentStep();
        return true;
    }

    private ProjectCreator buildCreator(Path path, Module module) {
        // TODO, now only Bukkit + Maven
        return new BukkitProjectCreator.BukkitMavenCreator(path, module, this);
    }

    private WorkLogStep newLog(Object object, List<WorkLogStep> steps) {
        final WorkLogStep step = new WorkLogStep(object);
        WorkLogStep.currentStep = step;
        steps.add(step);
        return step;
    }

    private void configureSystem() {

    }

    public enum ProjectType {

        MAVEN,
        GRADLE,
        KOTLIN_GRADLE;

    }

    public enum Platform {

        SPIGOT,
        BUNGEE,
        INDEPENDENT,
        DISCORD_BOT

    }

    public static class WorkLogStep {

        public static WorkLogStep currentStep;

        private Object config;
        private List<Pair<Object, Integer>> steps = new ArrayList<>();
        private Pair<Object, Integer> current = null;

        public WorkLogStep(Object config) {
            this.config = config;
        }

        public void printStep(StringBuilder stringBuilder) {
            stringBuilder.append("    ").append(config instanceof String ? config : config.getClass().getSimpleName()).append("\n");
            for (Pair<Object, Integer> pair : this.steps) {
                printStep(stringBuilder, pair.getLeft(), "        ", pair.getRight());
            }
            if (this.current != null) {
                printStep(stringBuilder, this.current.getLeft(), "        ", this.current.getRight());
            }
        }

        private void printStep(StringBuilder stringBuilder, Object step, String baseIndent, Integer indent) {
            for (int i = 0; i < indent; i++) {
                stringBuilder.append("    ");
            }
            stringBuilder.append(baseIndent).append(step instanceof String ? step : step.getClass().getName());
        }

        public void newCurrentStep(Object newStep) {
            this.newCurrentStep(newStep, 0);
        }

        public void newCurrentStep(Object newStep, int indent) {
            this.finishCurrentStep();
            this.current = Pair.of(newStep, indent);
        }

        public void finishCurrentStep() {
            if (this.current != null) {
                steps.add(current);
            }
            current = null;
        }

    }

    @Data
    @AllArgsConstructor
    public static class BuildDependency {
        private String groupId, artifactId, version;
        @Nullable
        private String mavenScope, gradleConfiguration;
    }

    @Data
    @AllArgsConstructor
    public static class BuildRepository {
        private String id;
        private String url;
    }

}

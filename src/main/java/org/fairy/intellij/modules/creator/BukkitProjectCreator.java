package org.fairy.intellij.modules.creator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fairy.intellij.FairyIntelliJ;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.modules.creator.step.*;
import org.fairy.intellij.modules.template.BukkitTemplate;
import org.fairy.intellij.util.GradleFiles;
import org.fairy.intellij.modules.creator.step.*;
import org.fairy.intellij.modules.exception.ModuleBuilderException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class BukkitProjectCreator extends BaseProjectCreator {

    private final Path directory;

    public BukkitProjectCreator(Path directory, Module module, FairyProjectSystem projectSystem) {
        super(module, projectSystem);

        this.directory = directory;
    }

    protected BasicJavaClassStep createMainClassStep() throws IOException {
        return createJavaClassStep(this.getProjectSystem().getMainClass(), (packageName, className) -> {
            return BukkitTemplate.INSTANCE.applyMainClass(this.getProject(), packageName, className, this.getProjectSystem(), false);
        });
    }

    protected BukkitDependenciesStep createDependenciesStep() {
        String mcVersion = "1.16.5";
        return new BukkitDependenciesStep(this.getProjectSystem(), mcVersion);
    }

    public static class BukkitKotlinGradleCreator extends BukkitProjectCreator {

        public BukkitKotlinGradleCreator(Path directory, Module module, FairyProjectSystem projectSystem) {
            super(directory, module, projectSystem);
        }

        private BasicKotlinClassStep createKotlinMainClassStep() throws IOException {
            return createKotlinClassStep(this.getProjectSystem().getMainClass(), (packageName, className) -> {
                return BukkitTemplate.INSTANCE.applyMainClass(this.getProject(), packageName, className, this.getProjectSystem(), true);
            });
        }

        @Override
        public List<CreatorStep> singleModule() {
            try {
                final String buildGradle = BukkitTemplate.INSTANCE.applyBuildGradleKts(this.getProject(), this.getProjectSystem());
                final String gradleProperties = BukkitTemplate.INSTANCE.applyGradleProperties(this.getProject());
                final String settingsGradle = BukkitTemplate.INSTANCE.applySettingsGradleKts(this.getProject(), this.getProjectSystem());

                GradleFiles<String> gradleFiles = new GradleFiles<>(buildGradle, gradleProperties, settingsGradle);

                return Arrays.asList(
                        this.createDependenciesStep(),
                        new KotlinDependenciesStep(this.getProjectSystem()),
                        new CreateDirectoriesStep(this.getProjectSystem(), this.getDirectory(), true),
                        new KotlinGradleSetupStep(this.getProject(), this.getDirectory(), this.getProjectSystem(), gradleFiles),
                        this.createKotlinMainClassStep(),
                        new GradleWrapperStep(this.getProject(), this.getDirectory(), this.getProjectSystem()),
                        new GitIgnoreStep(this.getProject(), this.getDirectory(), FairyProjectSystem.ProjectType.GRADLE),
                        new BasicGradleFinializerStep(this.getModule(), this.getDirectory(), this.getProjectSystem())
                );
            } catch (IOException e) {
                throw new ModuleBuilderException("An error occurs while creating Single Module step for Bukkit Gradle", e);
            }
        }

        @Override
        public List<CreatorStep> multipleModules() {
            return null;
        }
    }

    public static class BukkitGradleCreator extends BukkitProjectCreator {

        public BukkitGradleCreator(Path directory, Module module, FairyProjectSystem projectSystem) {
            super(directory, module, projectSystem);
        }

        @Override
        public List<CreatorStep> singleModule() {
            try {
                final String buildGradle = BukkitTemplate.INSTANCE.applyBuildGradle(this.getProject(), this.getProjectSystem());
                final String gradleProperties = BukkitTemplate.INSTANCE.applyGradleProperties(this.getProject());
                final String settingsGradle = BukkitTemplate.INSTANCE.applySettingsGradle(this.getProject(), this.getProjectSystem());

                GradleFiles<String> gradleFiles = new GradleFiles<>(buildGradle, gradleProperties, settingsGradle);

                return Arrays.asList(
                        this.createDependenciesStep(),
                        new CreateDirectoriesStep(this.getProjectSystem(), this.getDirectory(), false),
                        new GroovyGradleSetupStep(this.getProject(), this.getDirectory(), this.getProjectSystem(), gradleFiles),
                        this.createMainClassStep(),
                        new GradleWrapperStep(this.getProject(), this.getDirectory(), this.getProjectSystem()),
                        new GitIgnoreStep(this.getProject(), this.getDirectory(), FairyProjectSystem.ProjectType.GRADLE),
                        new BasicGradleFinializerStep(this.getModule(), this.getDirectory(), this.getProjectSystem())
                );
            } catch (IOException e) {
                throw new ModuleBuilderException("An error occurs while creating Single Module step for Bukkit Gradle", e);
            }
        }

        @Override
        public List<CreatorStep> multipleModules() {
            return null;
        }
    }

    public static class BukkitMavenCreator extends BukkitProjectCreator {

        public BukkitMavenCreator(Path directory, Module module, FairyProjectSystem projectSystem) {
            super(directory, module, projectSystem);
        }

        @Override
        public List<CreatorStep> singleModule() {
            String pomContent;
            try {
                pomContent = BukkitTemplate.INSTANCE.applyPom(this.getProject());
            } catch (IOException ex) {
                throw new IllegalArgumentException("An error occurs while applying POM", ex);
            }
            try {
                return Arrays.asList(
                        // Create Dependencies
                        this.createDependenciesStep(),

                        // Setup Maven Project, POM.xml etc
                        new BasicMavenStep(this.getProject(), this.getDirectory(), this.getProjectSystem(), pomContent),

                        // Create Main Class
                        this.createMainClassStep(),

                        // Setup .gitignore
                        new GitIgnoreStep(this.getProject(), this.getDirectory(), FairyProjectSystem.ProjectType.MAVEN),

                        // Let IntelliJ know the project is maven
                        new BasicMavenFinalizerStep(this.getModule(), this.getDirectory())
                        );
            } catch (Throwable throwable) {
                throw new IllegalArgumentException("An error occurs while setting up bukkit maven steps", throwable);
            }
        }

        @Override
        public List<CreatorStep> multipleModules() {
            return null;
        }
    }

    @RequiredArgsConstructor
    private static class BukkitDependenciesStep implements CreatorStep {

        private final FairyProjectSystem projectSystem;
        private final String mcVersion;

        @Override
        public void run(ProgressIndicator indicator) {
            this.projectSystem.getBuildRepositories().add(FairyProjectSystem.MAVEN_CENTRAL);
            this.projectSystem.getBuildRepositories().add(FairyProjectSystem.MAVEN_LOCAL);
            this.projectSystem.getBuildRepositories().add(new FairyProjectSystem.BuildRepository(
                    "codemc-repo",
                    "https://repo.codemc.io/repository/maven-snapshots/",

                    FairyProjectSystem.ProjectType.GRADLE,
                    FairyProjectSystem.ProjectType.KOTLIN_GRADLE
            ));

            this.projectSystem.getBuildRepositories().add(
                    new FairyProjectSystem.BuildRepository(
                            "papermc-repo",
                            "https://papermc.io/repo/repository/maven-public/"
                    )
            );
            this.projectSystem.getBuildDependencies().add(
                    new FairyProjectSystem.BuildDependency(
                            "com.destroystokyo.paper",
                            "paper-api",
                            mcVersion + "-R0.1-SNAPSHOT",
                            "provided",
                            "compileOnly"
                    )
            );

            // Imanity Libraries
            this.projectSystem.getBuildRepositories().add(
                    new FairyProjectSystem.BuildRepository(
                            "imanity-libraries",
                            "https://maven.imanity.dev/repository/imanity-libraries/"
                    )
            );
            this.projectSystem.getBuildDependencies().add(
                    new FairyProjectSystem.BuildDependency(
                            "org.fairy",
                            "bukkit-all",
                            FairyIntelliJ.getLatestFrameworkVersion(),
                            "provided",
                            "compileOnly"
                    )
            );
            this.projectSystem.getBuildDependencies().add(
                    new FairyProjectSystem.BuildDependency(
                            "org.fairy",
                            "bukkit-all",
                            FairyIntelliJ.getLatestFrameworkVersion(),
                            "none",
                            "annotationProcessor",
                            "kapt",

                            FairyProjectSystem.ProjectType.GRADLE,
                            FairyProjectSystem.ProjectType.KOTLIN_GRADLE
                    )
            );
        }
    }

}

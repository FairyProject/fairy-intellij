package org.imanity.framework.intellij.modules.creator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.modules.creator.step.*;
import org.imanity.framework.intellij.modules.template.BukkitTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class BukkitProjectCreator extends BaseProjectCreator {

    private final Path directory;

    public BukkitProjectCreator(Path directory, Module module, FrameworkProjectSystem projectSystem) {
        super(module, projectSystem);

        this.directory = directory;
    }

    protected BasicJavaClassStep createMainClassStep() throws IOException {
        return createJavaClassStep(this.getProjectSystem().getMainClass(), (packageName, className) -> {
            return BukkitTemplate.INSTANCE.applyMainClass(this.getProject(), packageName, className, this.getProjectSystem());
        });
    }

    protected BukkitDependenciesStep createDependenciesStep() {
        String mcVersion = "1.16.5";
        return new BukkitDependenciesStep(this.getProjectSystem(), mcVersion);
    }

    public static class BukkitMavenCreator extends BukkitProjectCreator {

        public BukkitMavenCreator(Path directory, Module module, FrameworkProjectSystem projectSystem) {
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
                        new MavenGitIgnoreStep(this.getProject(), this.getDirectory()),

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

        private final FrameworkProjectSystem projectSystem;
        private final String mcVersion;

        @Override
        public void run(ProgressIndicator indicator) {
            this.projectSystem.getRepositories().add(
                    new FrameworkProjectSystem.BuildRepository(
                            "papermc-repo",
                            "https://papermc.io/repo/repository/maven-public/"
                    )
            );
            this.projectSystem.getDependencies().add(
                    new FrameworkProjectSystem.BuildDependency(
                            "com.destroystokyo.paper",
                            "paper-api",
                            mcVersion + "-R0.1-SNAPSHOT",
                            "provided",
                            "compileOnly"
                    )
            );

            // Imanity Libraries
            this.projectSystem.getRepositories().add(
                    new FrameworkProjectSystem.BuildRepository(
                            "imanity-libraries",
                            "https://maven.imanity.dev/repository/imanity-libraries/"
                    )
            );
            this.projectSystem.getDependencies().add(
                    new FrameworkProjectSystem.BuildDependency(
                            "org.imanity.framework",
                            "bukkit-core",
                            "0.3b2", // TODO - Fetch latest version
                            "provided",
                            "compileOnly"
                    )
            );
        }
    }

}

package org.imanity.framework.intellij.modules.creator.step;

import com.intellij.openapi.progress.ProgressIndicator;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;

@RequiredArgsConstructor
public class KotlinDependenciesStep implements CreatorStep {

    private final FrameworkProjectSystem projectSystem;

    @Override
    public void run(ProgressIndicator indicator) {
        projectSystem.getBuildDependencies()
                .add(new FrameworkProjectSystem.BuildDependency(
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-jdk8",
                        "1.4.32",
                        "none",
                        "implementation",
                        FrameworkProjectSystem.ProjectType.KOTLIN_GRADLE
                ));
    }
}

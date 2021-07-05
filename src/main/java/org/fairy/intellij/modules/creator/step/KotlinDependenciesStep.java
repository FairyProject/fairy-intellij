package org.fairy.intellij.modules.creator.step;

import com.intellij.openapi.progress.ProgressIndicator;
import lombok.RequiredArgsConstructor;
import org.fairy.intellij.modules.FairyProjectSystem;

@RequiredArgsConstructor
public class KotlinDependenciesStep implements CreatorStep {

    private final FairyProjectSystem projectSystem;

    @Override
    public void run(ProgressIndicator indicator) {
        projectSystem.getBuildDependencies()
                .add(new FairyProjectSystem.BuildDependency(
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-jdk8",
                        "1.4.32",
                        "none",
                        "implementation",
                        FairyProjectSystem.ProjectType.KOTLIN_GRADLE
                ));
    }
}

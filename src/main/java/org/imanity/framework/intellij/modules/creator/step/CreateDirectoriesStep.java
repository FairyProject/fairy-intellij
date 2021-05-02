package org.imanity.framework.intellij.modules.creator.step;

import com.intellij.openapi.progress.ProgressIndicator;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.modules.exception.ModuleBuilderException;
import org.imanity.framework.intellij.util.DirectorySet;

import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
public class CreateDirectoriesStep implements CreatorStep {

    private final FrameworkProjectSystem projectSystem;
    private final Path directory;

    private final boolean kotlin;

    @Override
    public void run(ProgressIndicator indicator) {
        try {
            if (kotlin) {
                this.projectSystem.setDirectory(DirectorySet.createKotlin(this.directory));
                return;
            }
            this.projectSystem.setDirectory(DirectorySet.createJava(this.directory));
        } catch (IOException e) {
            throw new ModuleBuilderException("An error occurs while setting directory for project creation", e);
        }
    }
}

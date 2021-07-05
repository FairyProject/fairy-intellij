package org.fairy.intellij.modules.creator.step;

import com.intellij.openapi.progress.ProgressIndicator;
import lombok.RequiredArgsConstructor;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.util.DirectorySet;
import org.fairy.intellij.modules.exception.ModuleBuilderException;

import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
public class CreateDirectoriesStep implements CreatorStep {

    private final FairyProjectSystem projectSystem;
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

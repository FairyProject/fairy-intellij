package org.imanity.framework.intellij.modules.creator.step;

import com.intellij.ide.util.EditorHelper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.modules.exception.ModuleBuilderException;
import org.imanity.framework.intellij.util.ApplicationUtil;
import org.imanity.framework.intellij.util.DirectorySet;
import org.imanity.framework.intellij.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class BasicJavaClassStep implements CreatorStep {

    private final Project project;
    private final FrameworkProjectSystem projectCreator;
    private final String className;
    private final String content;

    @Override
    public void run(ProgressIndicator indicator) {
        final DirectorySet directory = this.projectCreator.getDirectoryOrError();

        ApplicationUtil.runWriteTask(() -> {
            indicator.setText("Writing class: " + this.className);
            final String[] fileSplit = className.split("\\.");
            final String className = fileSplit[fileSplit.length - 1];

            final Path sourceDir = this.getMainClassDirectory(directory.getSource(), fileSplit);
            final VirtualFile virtualFile;
            try {
                virtualFile = FileUtil.writeTextToFile(project, sourceDir, className + ".java", this.content);
            } catch (IOException ex) {
                throw new ModuleBuilderException("An error occurs while writing files for " + className + ".java", ex);
            }

            // Set the editor focus on the created class
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile != null) {
                EditorHelper.openInEditor(psiFile);
            }
        });
    }

    public Path getMainClassDirectory(Path dir, String... files) {
        return this.getMainClassDirectory(dir, Arrays.asList(files));
    }

    public Path getMainClassDirectory(Path dir, List<String> files) {
        final String[] array = files.subList(0, files.size() - 1).toArray(new String[0]);
        final Path outputDir = Paths.get(dir.toAbsolutePath().toString(), array);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ModuleBuilderException("An error occurs while creating directories for " + outputDir.toString(), e);
        }
        return outputDir;
    }
}

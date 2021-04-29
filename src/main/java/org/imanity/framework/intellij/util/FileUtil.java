package org.imanity.framework.intellij.util;

import com.google.common.base.Charsets;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import lombok.experimental.UtilityClass;
import org.imanity.framework.intellij.modules.creator.PendingReformatFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@UtilityClass
public class FileUtil {

    public VirtualFile writeTextToFile(Project project, Path targetDir, String fileName, String text) throws IOException {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        final Path file = targetDir.resolve(fileName);
        Files.write(file, text.getBytes(Charsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        final VirtualFile virtualFile = PathUtil.findVirtualFileOrError(file);

        ApplicationUtil.runReadAction(() -> {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile != null) {
                PendingReformatFiles.add(PsiUtil.createSmartPointer(project, psiFile));
            }
        });

        return virtualFile;
    }
}

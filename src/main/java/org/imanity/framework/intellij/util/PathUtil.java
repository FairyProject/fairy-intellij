package org.imanity.framework.intellij.util;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;

@UtilityClass
public class PathUtil {

    public VirtualFile findVirtualFileOrError(Path path) {
        final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path.toAbsolutePath().toString());
        if (virtualFile == null) {
            throw new IllegalStateException("Failed to find file: " + path.toString());
        }
        return virtualFile;
    }

}

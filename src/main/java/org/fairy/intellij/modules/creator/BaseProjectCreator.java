package org.fairy.intellij.modules.creator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.modules.creator.step.BasicJavaClassStep;
import org.fairy.intellij.modules.creator.step.BasicKotlinClassStep;
import org.fairy.intellij.util.ClassContentMapper;

import java.io.IOException;

@RequiredArgsConstructor
@Getter
public abstract class BaseProjectCreator implements ProjectCreator {

    private final Module module;
    private final FairyProjectSystem projectSystem;

    public Project getProject() {
        return this.module.getProject();
    }

    protected BasicJavaClassStep createJavaClassStep(String fullClassName, ClassContentMapper contentMapper) throws IOException {
        final Pair<String, String> pair = this.splitPackage(fullClassName);
        String packageName = pair.getKey();
        String className = pair.getValue();

        final String content = contentMapper.apply(packageName, className);
        return new BasicJavaClassStep(this.getProject(), this.projectSystem, fullClassName, content);
    }

    protected BasicKotlinClassStep createKotlinClassStep(String fullClassName, ClassContentMapper contentMapper) throws IOException {
        final Pair<String, String> pair = this.splitPackage(fullClassName);
        String packageName = pair.getKey();
        String className = pair.getValue();

        final String content = contentMapper.apply(packageName, className);
        return new BasicKotlinClassStep(this.getProject(), this.projectSystem, fullClassName, content);
    }

    protected Pair<String, String> splitPackage(String fullClassName) {
        final int index = fullClassName.lastIndexOf('.');
        return Pair.of(fullClassName.substring(0, index), fullClassName.substring(index + 1));
    }

}

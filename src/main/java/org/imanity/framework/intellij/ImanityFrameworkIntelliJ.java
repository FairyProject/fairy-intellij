package org.imanity.framework.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import lombok.experimental.UtilityClass;
import org.imanity.framework.intellij.constant.ClassConstants;

import java.util.Arrays;

@UtilityClass
public class ImanityFrameworkIntelliJ {

    public boolean isBean(PsiType type) {
        final PsiClass psiClass = PsiTypesUtil.getPsiClass(type);
        if (psiClass == null) {
            return false;
        }
        return psiClass.hasAnnotation(ClassConstants.COMPONENT_CLASS) || psiClass.hasAnnotation(ClassConstants.SERVICE_CLASS);
    }

}

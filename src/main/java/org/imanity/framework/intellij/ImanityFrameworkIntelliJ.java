package org.imanity.framework.intellij;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import lombok.experimental.UtilityClass;
import org.imanity.framework.intellij.constant.ClassConstants;

@UtilityClass
public class ImanityFrameworkIntelliJ {

    public static final String DEFAULT_WRAPPER_VERSION = "5.6.1";

    public boolean isBean(PsiType type) {
        final PsiClass psiClass = PsiTypesUtil.getPsiClass(type);
        if (psiClass == null) {
            return false;
        }
        return psiClass.hasAnnotation(ClassConstants.COMPONENT_CLASS) || psiClass.hasAnnotation(ClassConstants.SERVICE_CLASS);
    }

    public String getLatestFrameworkVersion() {
        // TODO - Cloud fetch
        return "0.3b2";
    }

}

package org.fairy.intellij;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import lombok.experimental.UtilityClass;
import org.fairy.intellij.constant.ClassConstants;

@UtilityClass
public class FairyIntelliJ {

    public static final String DEFAULT_WRAPPER_VERSION = "7.1";

    public boolean isBean(PsiType type) {
        final PsiClass psiClass = PsiTypesUtil.getPsiClass(type);
        if (psiClass == null) {
            return false;
        }
        return psiClass.hasAnnotation(ClassConstants.COMPONENT_CLASS) || psiClass.hasAnnotation(ClassConstants.SERVICE_CLASS);
    }

    public String getLatestFrameworkVersion() {
        // TODO - Cloud fetch
        return "0.4b1";
    }

}

package org.fairy.intellij.inspection;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.fairy.intellij.constant.ClassConstants;
import org.fairy.intellij.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ReflectSuppressor implements InspectionSuppressor {
    public boolean shouldSuppress(PsiElement element) {
        PsiClass clazz = PsiUtil.findContainingClass(element);
        if (clazz == null) {
            return false;
        }

        boolean shouldSurpass = false;
        if (clazz.hasAnnotation(ClassConstants.ENTITY_CLASS)) {
            shouldSurpass = true;
        }

        if (clazz.hasAnnotation(ClassConstants.CONFIGURATION_ELEMENT_CLASS)) {
            shouldSurpass = true;
        }

        final PsiClass superClass = clazz.getSuperClass();
        if (superClass != null) {
            final String name = superClass.getQualifiedName();
            System.out.println(name);
            if (name != null && name.equals(ClassConstants.YAML_CONFIGURATION_CLASS)) {
                shouldSurpass = true;
            }
        }
        return shouldSurpass;
    }

    @NotNull
    @Override
    public SuppressQuickFix [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}

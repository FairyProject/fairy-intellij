package org.imanity.framework.intellij.inspection;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.imanity.framework.intellij.constant.ClassConstants;
import org.imanity.framework.intellij.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldMayBeFinalSuppressor implements InspectionSuppressor {
    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!toolId.equals("FieldMayBeFinal")) {
            return false;
        }

        PsiClass clazz = PsiUtil.findContainingClass(element);
        if (clazz == null) {
            return false;
        }

        return clazz.hasAnnotation(ClassConstants.ENTITY_CLASS);
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}

package org.imanity.framework.intellij.inspection;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.*;
import org.imanity.framework.intellij.constant.ClassConstants;
import org.imanity.framework.intellij.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnusedDeclarationSuppressor implements InspectionSuppressor {

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!toolId.equals("UnusedDeclaration")) {
            return false;
        }

        final PsiField field = PsiUtil.findContainingField(element);
        if (field == null) {
            return false;
        }

        return field.hasAnnotation(ClassConstants.AUTOWIRED_CLASS);
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return SuppressQuickFix.EMPTY_ARRAY;
    }
}

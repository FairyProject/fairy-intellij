package org.fairy.intellij.inspection;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class FieldMayBeFinalSuppressor extends ReflectSuppressor {
    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!toolId.equals("FieldMayBeFinal")) {
            return false;
        }

        return this.shouldSuppress(element);
    }
}

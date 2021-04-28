package org.imanity.framework.intellij.util;

import com.intellij.psi.*;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class PsiUtil {

    public PsiClass findContainingClass(PsiElement element) {
        return findParent(element, PsiClass.class, el -> false);
    }

    public PsiField findContainingField(PsiElement element) {
        return findParent(element, PsiField.class, el -> el instanceof PsiClass);
    }

    public <T extends PsiElement> T findParent(PsiElement element, Class<T> type, Function<PsiElement, Boolean> stop) {
        PsiElement el = element;

        while (true) {
            if (type.isInstance(el)) {
                return type.cast(el);
            }

            if (el instanceof PsiFile || el instanceof PsiDirectory || stop.apply(el)) {
                return null;
            }

            el = el.getParent();
            if (el == null) {
                return null;
            }
        }
    }

}

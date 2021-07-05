package org.fairy.intellij.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
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

    public <T extends PsiElement> SmartPsiElementPointer<T> createSmartPointer(Project theProject, T element) {
        return SmartPointerManager.getInstance(theProject).createSmartPsiElementPointer(element);
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

    public void runWriteAction(PsiFile file, Runnable runnable) throws Throwable {
        WriteCommandAction.writeCommandAction(file).withGlobalUndo().compute((ThrowableComputable<Object, Throwable>) () -> {
            runnable.run();
            return null;
        });
        final Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) {
            return;
        }
        PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(document);
    }

    public <T extends PsiElement> T getPsiChildrenByType(PsiElement parent, Class<T> type) {
        for (PsiElement child : parent.getChildren()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }

        return null;
    }

}

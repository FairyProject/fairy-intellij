package org.fairy.intellij.modules.creator;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import lombok.experimental.UtilityClass;
import org.fairy.intellij.util.ApplicationUtil;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PendingReformatFiles {

    private final List<SmartPsiElementPointer<PsiFile>> PENDING_REFORMAT_FILES = new ArrayList<>();

    public void reformatAll() {
        ApplicationUtil.runWriteTask(() -> {
            for (SmartPsiElementPointer<PsiFile> pendingReformatFile : PENDING_REFORMAT_FILES) {
                final PsiFile element = pendingReformatFile.getElement();
                if (element == null) {
                    continue;
                }

                new ReformatCodeProcessor(element, false).run();
            }
        });
        PENDING_REFORMAT_FILES.clear();
    }

    public void add(SmartPsiElementPointer<PsiFile> pointer) {
        PENDING_REFORMAT_FILES.add(pointer);
    }

}

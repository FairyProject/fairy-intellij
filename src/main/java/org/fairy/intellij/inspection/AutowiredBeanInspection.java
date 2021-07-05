package org.fairy.intellij.inspection;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.psi.PsiField;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import org.fairy.intellij.FairyIntelliJ;
import org.fairy.intellij.constant.ClassConstants;
import org.jetbrains.annotations.NotNull;

public class AutowiredBeanInspection extends BaseInspection {

    @Override
    public @NotNull String getDisplayName() {
        return "Autowired target is not a bean.";
    }

    @Override
    protected @NotNull @InspectionMessage String buildErrorString(Object... infos) {
        if (infos.length > 0 && infos[0] instanceof String) {
            return (String) infos[0];
        }
        return "";
    }

    @Override
    public BaseInspectionVisitor buildVisitor() {
        return new BaseInspectionVisitor() {
            @Override
            public void visitField(PsiField field) {
                if (!field.hasAnnotation(ClassConstants.AUTOWIRED_CLASS)) {
                    return;
                }
                if (FairyIntelliJ.isBean(field.getType())) {
                    return;
                }
                registerFieldError(field, field.getType().getCanonicalText() + " is not a verified bean.");
            }

        };
    }
}

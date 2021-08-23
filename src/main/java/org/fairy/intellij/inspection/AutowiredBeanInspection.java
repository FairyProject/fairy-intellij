package org.fairy.intellij.inspection;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
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
                PsiType type = field.getType();
                final PsiClass psiClass = PsiTypesUtil.getPsiClass(type);
                final String qualifiedName = psiClass.getQualifiedName();
                if (type instanceof PsiClassType
                        && ((PsiClassType) type).hasParameters()
                        && qualifiedName != null
                        && (qualifiedName.equals(ClassConstants.OPTIONAL_CLASS) || qualifiedName.equals(ClassConstants.BEAN_HOLDER_CLASS))) {
                    type = ((PsiClassType) type).getParameters()[0];
                }
                if (FairyIntelliJ.isBean(type)) {
                    return;
                }
                registerFieldError(field, type.getCanonicalText() + " is not a verified bean.");
            }

        };
    }
}

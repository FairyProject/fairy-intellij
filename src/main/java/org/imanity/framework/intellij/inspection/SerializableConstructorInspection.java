package org.imanity.framework.intellij.inspection;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import org.imanity.framework.intellij.constant.ClassConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class SerializableConstructorInspection extends BaseInspection {

    @Override
    public @NotNull String getDisplayName() {
        return "@Entity Class doesn't have no args constructor";
    }

    @Override
    protected @NotNull @InspectionMessage String buildErrorString(Object... infos) {
        return (String) infos[0];
    }

    @Override
    protected @Nullable InspectionGadgetsFix buildFix(Object... infos) {
        final PsiClass type = (PsiClass) infos[1];

        return new InspectionGadgetsFix() {
            @Override
            protected void doFix(Project project, ProblemDescriptor descriptor) {
                final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);

                final PsiClass lombokNoArgs = psiFacade.findClass(ClassConstants.LOMBOK_NO_ARGS_CONSTRUCTOR_CLASS, GlobalSearchScope.allScope(project));
                if (lombokNoArgs != null) {
                    injectImport(elementFactory, type, lombokNoArgs);
                    injectAnnotation(elementFactory, type);
                } else {
                    String code = "private " + type.getName() + "() {}";
                    final PsiMethod method = elementFactory.createMethodFromText(code, type);

                    type.add(method);
                }
            }

            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return "Create no args constructor";
            }
        };
    }

    private void injectImport(PsiElementFactory factory, PsiClass type, PsiClass toImport) {
        final PsiFile file = type.getContainingFile();
        if (!(file instanceof PsiJavaFile)) {
            return;
        }

        PsiJavaFile javaFile = (PsiJavaFile) file;
        if (javaFile.findImportReferenceTo(toImport) != null) {
            return;
        }

        final PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return;
        }

        importList.add(factory.createImportStatement(toImport));
    }

    private void injectAnnotation(PsiElementFactory factory, PsiClass type) {
        if (factory == null || type == null) {
            return;
        }
        PsiModifierList modifierList = type.getModifierList();
        if (modifierList != null) {
            PsiElement firstChild = modifierList.getFirstChild();
            Pattern pattern = Pattern.compile("@.*?NoArgsConstructor");
            if (firstChild != null && !pattern.matcher(firstChild.getText()).find()) {
                PsiAnnotation annotationFromText =
                        factory.createAnnotationFromText("@NoArgsConstructor", type);
                modifierList.addBefore(annotationFromText, firstChild);
            }
        }
    }

    @Override
    public BaseInspectionVisitor buildVisitor() {
        return new BaseInspectionVisitor() {
            @Override
            public void visitClass(PsiClass aClass) {
                if (!aClass.hasAnnotation(ClassConstants.ENTITY_CLASS)) {
                    return;
                }

                boolean hasCodedConstructor = false;

                for (PsiMethod constructor : aClass.getConstructors()) {
                    hasCodedConstructor = true;
                    if (constructor.getParameterList().getParametersCount() == 0) {
                        return;
                    }
                }

                if (!hasCodedConstructor) {
                    return;
                }

                this.registerClassError(aClass, "This Class marked as @Entity but doesn't have a no args constructor!", aClass);
            }
        };
    }
}

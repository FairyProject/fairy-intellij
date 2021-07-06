package org.fairy.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import lombok.experimental.UtilityClass;
import org.fairy.intellij.constant.ClassConstants;

@UtilityClass
public class FairyIntelliJ {

    public static final String DEFAULT_WRAPPER_VERSION = "7.1";

    public boolean isBean(PsiType type) {
        final PsiClass abstractPlugin;

        final ProjectManager projectManager = ProjectManager.getInstanceIfCreated();
        if (projectManager != null && projectManager.getOpenProjects().length > 0) {
            Project project = projectManager.getOpenProjects()[0];
            final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
            abstractPlugin = psiFacade.findClass(ClassConstants.ABSTRACT_PLUGIN_CLASS, GlobalSearchScope.allScope(project));
        }  else {
            abstractPlugin = null;
        }

        final PsiClass psiClass = PsiTypesUtil.getPsiClass(type);
        if (psiClass == null) {
            return false;
        }

        if (abstractPlugin != null && psiClass.isInheritor(abstractPlugin, true)) {
            return true;
        }

        // check every inherit class for Verified Classes
        return psiClass.hasAnnotation(ClassConstants.VERIFIED_CLASS) || psiClass.hasAnnotation(ClassConstants.COMPONENT_CLASS) || psiClass.hasAnnotation(ClassConstants.SERVICE_CLASS);
    }

    public String getLatestFrameworkVersion() {
        // TODO - Cloud fetch
        return "0.4b2";
    }

}

package org.fairy.intellij.modules.template;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public abstract class BaseTemplate {

    protected String applyTemplate(Project project, String templateName) throws IOException {
        return this.applyTemplate(project, templateName, Collections.emptyMap());
    }

    protected String applyTemplate(Project project, String templateName, Map<String, String> properties) throws IOException {
        final FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
        final FileTemplate j2eeTemplate = templateManager.getJ2eeTemplate(templateName);

        final Properties allProperties = templateManager.getDefaultProperties();
        if (properties != null) {
            allProperties.putAll(properties);
        }

        return j2eeTemplate.getText(allProperties);
    }

}

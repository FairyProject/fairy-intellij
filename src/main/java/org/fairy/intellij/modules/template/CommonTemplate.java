package org.fairy.intellij.modules.template;

import com.intellij.openapi.project.Project;

import java.io.IOException;

public class CommonTemplate extends BaseTemplate {

    public static final CommonTemplate INSTANCE = new CommonTemplate();

    public static final String MAVEN_GITIGNORE_TEMPLATE = "Maven.gitignore",
            GRADLE_GITIGNORE_TEMPLATE = "Gradle.gitignore";

    public String applyMavenGitIgnore(Project project) throws IOException {
        return this.applyTemplate(project, MAVEN_GITIGNORE_TEMPLATE);
    }

    public String applyGradleGitIgnore(Project project) throws IOException {
        return this.applyTemplate(project, GRADLE_GITIGNORE_TEMPLATE);
    }

}

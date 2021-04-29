package org.imanity.framework.intellij.modules.template;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import org.imanity.framework.intellij.ImanityFrameworkAssets;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class BukkitTemplate extends BaseTemplate {

    public static final BukkitTemplate INSTANCE = new BukkitTemplate();

    public static final String BUKKIT_MAIN_CLASS_TEMPLATE = "Bukkit Main Class.java",
            BUKKIT_POM_TEMPLATE = "Bukkit pom.xml";

    public String applyMainClass(Project project, String packageName, String className, FrameworkProjectSystem projectSystem) throws IOException {
        final Map<String, String> map = ImmutableMap.of(
                "PACKAGE", packageName,
                "CLASS_NAME", className,
                "NAME", projectSystem.getArtifactId(),
                "VERSION", projectSystem.getVersion(),
                "DESCRIPTION", "TODO" //TODO
        );

        return this.applyTemplate(project, BUKKIT_MAIN_CLASS_TEMPLATE, map);
    }

    public String applyPom(Project project) throws IOException {
        return this.applyTemplate(project, BUKKIT_POM_TEMPLATE, this.readMavenVersions());
    }

    private Map<String, String> readMavenVersions() {
        // TODO - download from cloud
        final InputStream inputStream = ImanityFrameworkAssets.class.getResourceAsStream("/assets/maven.json");
        if (inputStream == null) {
            throw new IllegalArgumentException("The Assets /assets/maven.json is null");
        }

        return new Gson().fromJson(new InputStreamReader(inputStream), Map.class);
    }

}

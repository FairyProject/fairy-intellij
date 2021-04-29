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
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("PACKAGE", packageName)
                .put("CLASS_NAME", className)
                .put("NAME", projectSystem.getName())
                .put("VERSION", projectSystem.getVersion())
                .put("DESCRIPTION", projectSystem.getDescription());

        // Depends
        if (projectSystem.getDependencies().length > 0 || projectSystem.getSoftDependencies().length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < projectSystem.getDependencies().length; i++) {
                String dependency = projectSystem.getDependencies()[i];

                stringBuilder.append("@PluginDependency(")
                        .append("\"")
                        .append(dependency)
                        .append("\"")
                        .append(")");
                if (i != projectSystem.getDependencies().length - 1 || projectSystem.getSoftDependencies().length > 0) {
                    stringBuilder.append(", ");
                }
            }

            for (int i = 0; i < projectSystem.getSoftDependencies().length; i++) {
                String dependency = projectSystem.getSoftDependencies()[i];

                stringBuilder.append("@PluginDependency(")
                        .append("\"")
                        .append(dependency)
                        .append("\"")
                        .append(", soft = true)");
                if (i != projectSystem.getSoftDependencies().length - 1) {
                    stringBuilder.append(", ");
                }
            }

            builder.put("DEPEND", stringBuilder.toString());
        }

        // Load Before
        if (projectSystem.getLoadBefore().length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < projectSystem.getLoadBefore().length; i++) {
                String dependency = projectSystem.getLoadBefore()[i];

                stringBuilder.append("\"").append(dependency).append("\"");
                if (i != projectSystem.getLoadBefore().length - 1) {
                    stringBuilder.append(", ");
                }
            }

            builder.put("LOAD_BEFORE", stringBuilder.toString());
        }

        // Authors
        if (projectSystem.getAuthors().length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < projectSystem.getAuthors().length; i++) {
                String dependency = projectSystem.getAuthors()[i];

                stringBuilder.append("\"").append(dependency).append("\"");
                if (i != projectSystem.getAuthors().length - 1) {
                    stringBuilder.append(", ");
                }
            }

            builder.put("AUTHOR", stringBuilder.toString());
        }

        builder.put("LOAD_ORDER", projectSystem.getLoadOrder());

        return this.applyTemplate(project, BUKKIT_MAIN_CLASS_TEMPLATE, builder.build());
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

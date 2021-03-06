package org.fairy.intellij.modules.template;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import org.fairy.intellij.modules.FairyProjectSystem;
import org.fairy.intellij.FairyAssets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class BukkitTemplate extends BaseTemplate {

    public static final BukkitTemplate INSTANCE = new BukkitTemplate();

    public static final String SPIGOT_MAIN_CLASS_TEMPLATE = "Spigot Main Class.java",
            SPIGOT_MAIN_CLASS_KOTLIN_TEMPLATE = "Spigot Main Class.kt",
            SPIGOT_POM_TEMPLATE = "Spigot pom.xml",
            SPIGOT_BUILD_GRADLE_TEMPLATE = "Spigot build.gradle",
            SPIGOT_GRADLE_PROPERTIES_TEMPLATE = "Spigot gradle.properties",
            SPIGOT_SETTINGS_GRADLE_TEMPLATE = "Spigot settings.gradle",
            SPIGOT_BUILD_GRADLE_KTS_TEMPLATE = "Spigot build.gradle.kts",
            SPIGOT_SETTINGS_GRADLE_KTS_TEMPLATE = "Spigot settings.gradle.kts";

    public String applyMainClass(Project project, String packageName, String className, FairyProjectSystem projectSystem, boolean kotlin) throws IOException {
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

        if (kotlin) {
            return this.applyTemplate(project, SPIGOT_MAIN_CLASS_KOTLIN_TEMPLATE, builder.build());
        }
        return this.applyTemplate(project, SPIGOT_MAIN_CLASS_TEMPLATE, builder.build());
    }

    public String applyPom(Project project) throws IOException {
        return this.applyTemplate(project, SPIGOT_POM_TEMPLATE, this.readMavenVersions());
    }

    public String applyGradleProperties(Project project) throws IOException {
        return this.applyTemplate(project, SPIGOT_GRADLE_PROPERTIES_TEMPLATE);
    }

    public String applySettingsGradle(Project project, FairyProjectSystem projectSystem) throws IOException {
        Map<String, String> properties = ImmutableMap.of("ARTIFACT_ID", projectSystem.getArtifactId());

        return this.applyTemplate(project, SPIGOT_SETTINGS_GRADLE_TEMPLATE, properties);
    }

    public String applySettingsGradleKts(Project project, FairyProjectSystem projectSystem) throws IOException {
        Map<String, String> properties = ImmutableMap.of("ARTIFACT_ID", projectSystem.getArtifactId());

        return this.applyTemplate(project, SPIGOT_SETTINGS_GRADLE_KTS_TEMPLATE, properties);
    }

    public String applyBuildGradle(Project project, FairyProjectSystem projectSystem) throws IOException {
        Map<String, String> map = ImmutableMap.of(
                "GROUP_ID", projectSystem.getGroupId(),
                "VERSION", projectSystem.getArtifactId()
        );

        return this.applyTemplate(project, SPIGOT_BUILD_GRADLE_TEMPLATE, map);
    }

    public String applyBuildGradleKts(Project project, FairyProjectSystem projectSystem) throws IOException {
        Map<String, String> map = ImmutableMap.of(
                "GROUP_ID", projectSystem.getGroupId(),
                "VERSION", projectSystem.getArtifactId()
        );

        return this.applyTemplate(project, SPIGOT_BUILD_GRADLE_KTS_TEMPLATE, map);
    }

    private Map<String, String> readMavenVersions() {
        // TODO - download from cloud
        final InputStream inputStream = FairyAssets.class.getResourceAsStream("/assets/maven.json");
        if (inputStream == null) {
            throw new IllegalArgumentException("The Assets /assets/maven.json is null");
        }

        return new Gson().fromJson(new InputStreamReader(inputStream), Map.class);
    }

}

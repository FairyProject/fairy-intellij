package org.fairy.intellij.modules.template;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import org.fairy.intellij.FairyAssets;

public class FrameworkTemplates implements FileTemplateGroupDescriptorFactory {
    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Framework", FairyAssets.FRAMEWORK_LOGO);

        final FileTemplateGroupDescriptor bukkitGroup = new FileTemplateGroupDescriptor("Bukkit", FairyAssets.SPIGOT_LOGO);
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_MAIN_CLASS_TEMPLATE));
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_MAIN_CLASS_KOTLIN_TEMPLATE));
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_POM_TEMPLATE));
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_BUILD_GRADLE_TEMPLATE));
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_GRADLE_PROPERTIES_TEMPLATE));
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_SETTINGS_GRADLE_TEMPLATE));
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_BUILD_GRADLE_KTS_TEMPLATE));
        group.addTemplate(bukkitGroup);

        final FileTemplateGroupDescriptor commonGroup = new FileTemplateGroupDescriptor("Common", FairyAssets.FRAMEWORK_LOGO);
        commonGroup.addTemplate(new FileTemplateDescriptor(CommonTemplate.MAVEN_GITIGNORE_TEMPLATE));
        commonGroup.addTemplate(new FileTemplateDescriptor(CommonTemplate.GRADLE_GITIGNORE_TEMPLATE));
        group.addTemplate(commonGroup);
        return group;
    }
}

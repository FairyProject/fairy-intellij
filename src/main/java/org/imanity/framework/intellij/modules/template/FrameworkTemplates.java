package org.imanity.framework.intellij.modules.template;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import org.imanity.framework.intellij.ImanityFrameworkAssets;

public class FrameworkTemplates implements FileTemplateGroupDescriptorFactory {
    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        System.out.println("yes");
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Framework", ImanityFrameworkAssets.FRAMEWORK_LOGO);

        final FileTemplateGroupDescriptor bukkitGroup = new FileTemplateGroupDescriptor("Bukkit", ImanityFrameworkAssets.SPIGOT_LOGO);
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_MAIN_CLASS_TEMPLATE));
        bukkitGroup.addTemplate(new FileTemplateDescriptor(BukkitTemplate.BUKKIT_POM_TEMPLATE));
        group.addTemplate(bukkitGroup);

        final FileTemplateGroupDescriptor commonGroup = new FileTemplateGroupDescriptor("Common", ImanityFrameworkAssets.FRAMEWORK_LOGO);
        commonGroup.addTemplate(new FileTemplateDescriptor(CommonTemplate.MAVEN_GITIGNORE_TEMPLATE));
        group.addTemplate(commonGroup);
        return group;
    }
}

package org.imanity.framework.intellij.modules.creator.step;

import com.google.common.collect.ImmutableList;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.intellij.modules.FrameworkProjectSystem;
import org.imanity.framework.intellij.modules.exception.ModuleBuilderException;
import org.imanity.framework.intellij.util.ApplicationUtil;
import org.imanity.framework.intellij.util.DirectorySet;
import org.imanity.framework.intellij.util.PathUtil;
import org.imanity.framework.intellij.util.PsiUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class BasicMavenStep implements CreatorStep {

    private final Project project;
    private final Path directory;
    private final FrameworkProjectSystem projectSystem;
    private final String pomContent;
    private final List<MavenStepFunction> mavenStepFunctions;

    public BasicMavenStep(Project project, Path directory, FrameworkProjectSystem projectSystem, String pomContent) {
        this.project = project;
        this.directory = directory;
        this.projectSystem = projectSystem;
        this.pomContent = pomContent;
        this.mavenStepFunctions = DEFAULT_SINGLE_STEPS;
    }

    @Override
    public void run(ProgressIndicator indicator) {
        try {
            Files.createDirectories(this.directory);
        } catch (IOException e) {
            throw new ModuleBuilderException("An exception occurs while creating directory for maven", e);
        }

        ApplicationUtil.runWriteTask(() -> {
            final PsiFile pomPsi = PsiFileFactory.getInstance(this.project).createFileFromText(XMLLanguage.INSTANCE, this.pomContent);
            if (pomPsi == null) {
                return;
            }

            pomPsi.setName("pom.xml");

            XmlFile pomXml = (XmlFile) pomPsi;
            try {
                PsiUtil.runWriteAction(pomPsi, () -> {
                    final DomManager domManager = DomManager.getDomManager(this.project);
                    final DomFileElement<MavenDomProjectModel> fileElement = domManager.getFileElement(pomXml, MavenDomProjectModel.class);
                    if (fileElement == null) {
                        return;
                    }

                    final MavenDomProjectModel mavenProjectXml = fileElement.getRootElement();
                    final XmlTag rootTag = pomXml.getRootTag();
                    if (rootTag == null) {
                        return;
                    }

                    for (MavenStepFunction mavenStepFunction : this.mavenStepFunctions) {
                        if (FrameworkProjectSystem.WorkLogStep.currentStep != null) {
                            FrameworkProjectSystem.WorkLogStep.currentStep.newCurrentStep(mavenStepFunction, 1);
                        }
                        mavenStepFunction.apply(this, mavenProjectXml, rootTag);
                    }

                    if (FrameworkProjectSystem.WorkLogStep.currentStep != null) {
                        FrameworkProjectSystem.WorkLogStep.currentStep.finishCurrentStep();
                    }

                    final VirtualFile vRootDir = PathUtil.findVirtualFileOrError(this.directory);
                    if (vRootDir == null) {
                        throw new IllegalStateException("Unable to find root directory " + this.directory);
                    }

                    final PsiDirectory directory = PsiManager.getInstance(this.project).findDirectory(vRootDir);
                    if (directory == null) {
                        return;
                    }

                    final PsiFile oldFile = directory.findFile(pomPsi.getName());
                    if (oldFile != null) {
                        oldFile.delete();
                    }
                    directory.add(pomPsi);

                    vRootDir.refresh(false, false);
                    final VirtualFile pomFile = vRootDir.findChild(pomPsi.getName());
                    if (pomFile == null) {
                        return;
                    }

                    final PsiFile file = PsiManager.getInstance(this.project).findFile(pomFile);
                    if (file != null) {
                        new ReformatCodeProcessor(file, false).run();
                    }
                });
            } catch (Throwable throwable) {
                throw new IllegalArgumentException("An error occurs while running write action for pom.xml", throwable);
            }
        });
    }


    public interface MavenStepFunction {

        void apply(BasicMavenStep step, MavenDomProjectModel mavenModel, XmlTag xmlTag);

    }

    public static final List<MavenStepFunction> DEFAULT_SINGLE_STEPS = ImmutableList.of(
            new MavenStepFunctionDirectory(),
            new MavenStepCore(),
            new MavenStepName(),
            new MavenStepInfo(),
            new MavenStepDependencies()
    );

    public static class MavenStepFunctionDirectory implements MavenStepFunction {

        @Override
        public void apply(BasicMavenStep step, MavenDomProjectModel mavenModel, XmlTag xmlTag) {
            try {
                step.getProjectSystem().setDirectory(DirectorySet.create(step.getDirectory()));
            } catch (IOException e) {
                throw new IllegalArgumentException("An error occurs while creating directory", e);
            }
        }
    }

    public static class MavenStepCore implements MavenStepFunction {

        @Override
        public void apply(BasicMavenStep step, MavenDomProjectModel mavenModel, XmlTag xmlTag) {
            mavenModel.getGroupId().setStringValue(step.getProjectSystem().getGroupId());
            mavenModel.getArtifactId().setStringValue(step.getProjectSystem().getArtifactId());
            mavenModel.getVersion().setStringValue(step.getProjectSystem().getVersion());
        }

    }

    public static class MavenStepName implements MavenStepFunction {
        @Override
        public void apply(BasicMavenStep step, MavenDomProjectModel mavenModel, XmlTag xmlTag) {
            mavenModel.getName().setStringValue(step.getProjectSystem().getName());
        }
    }

    public static class MavenStepInfo implements MavenStepFunction {
        @Override
        public void apply(BasicMavenStep step, MavenDomProjectModel mavenModel, XmlTag xmlTag) {
            mavenModel.getDescription().setStringValue(step.getProjectSystem().getDescription());
        }
    }

    public static class MavenStepDependencies implements MavenStepFunction {
        @Override
        public void apply(BasicMavenStep step, MavenDomProjectModel mavenModel, XmlTag xmlTag) {
            for (FrameworkProjectSystem.BuildRepository repository : step.getProjectSystem().getBuildRepositories()) {
                final MavenDomRepository mavenDomRepository = mavenModel.getRepositories().addRepository();
                mavenDomRepository.getId().setStringValue(repository.getId());
                mavenDomRepository.getUrl().setStringValue(repository.getUrl());
            }

            for (FrameworkProjectSystem.BuildDependency dependency : step.getProjectSystem().getBuildDependencies()) {
                if (!dependency.getTypes().contains(FrameworkProjectSystem.ProjectType.MAVEN)) {
                    continue;
                }
                final MavenDomDependency mavenDomDependency = mavenModel.getDependencies().addDependency();
                mavenDomDependency.getGroupId().setStringValue(dependency.getGroupId());
                mavenDomDependency.getArtifactId().setStringValue(dependency.getArtifactId());
                mavenDomDependency.getVersion().setStringValue(dependency.getVersion());
                mavenDomDependency.getScope().setStringValue(dependency.getMavenScope());
            }
        }
    }

}

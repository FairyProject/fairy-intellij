package org.imanity.framework.intellij.modules.creator;

import org.imanity.framework.intellij.modules.creator.step.CreatorStep;

import java.util.List;

public interface ProjectCreator {

    List<CreatorStep> singleModule();

    List<CreatorStep> multipleModules();

}

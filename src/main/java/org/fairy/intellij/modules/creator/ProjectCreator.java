package org.fairy.intellij.modules.creator;

import org.fairy.intellij.modules.creator.step.CreatorStep;

import java.util.List;

public interface ProjectCreator {

    List<CreatorStep> singleModule();

    List<CreatorStep> multipleModules();

}

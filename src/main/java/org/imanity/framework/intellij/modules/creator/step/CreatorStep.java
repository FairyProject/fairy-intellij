package org.imanity.framework.intellij.modules.creator.step;

import com.intellij.openapi.progress.ProgressIndicator;

public interface CreatorStep {

    void run(ProgressIndicator indicator);

}

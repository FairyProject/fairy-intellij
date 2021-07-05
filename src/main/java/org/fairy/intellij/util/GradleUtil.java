package org.fairy.intellij.util;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.StatusBarEx;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@UtilityClass
public class GradleUtil {

    public void runGradleTaskAndWait(Project project, Path directory, Consumer<ExternalSystemTaskExecutionSettings> consumer) {
        final CountDownLatch latch = new CountDownLatch(1);

        runGradleTaskWithCallback(project, directory, consumer, new GradleCallback(latch));

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Thread Interrupted", e);
        }
    }

    public void showProgress(Project project) {
        if (!UISettings.getInstance().getShowStatusBar() || UISettings.getInstance().getPresentationMode()) {
            return;
        }

        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (!(statusBar instanceof StatusBarEx)) {
            return;
        }
        ((StatusBarEx) statusBar).setProcessWindowOpen(true);
    }

    private void runGradleTaskWithCallback(Project project, Path directory, Consumer<ExternalSystemTaskExecutionSettings> consumer, TaskCallback callback) {
        final ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();
        settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.getId());
        settings.setExternalProjectPath(directory.toString());
        consumer.accept(settings);

        ExternalSystemUtil.runTask(settings,
                DefaultRunExecutor.EXECUTOR_ID,
                project,
                GradleConstants.SYSTEM_ID,
                callback,
                ProgressExecutionMode.NO_PROGRESS_ASYNC,
                false);
    }

    @RequiredArgsConstructor
    public class GradleCallback implements TaskCallback {

        private final CountDownLatch latch;

        @Override
        public void onSuccess() {
            this.call();
        }

        @Override
        public void onFailure() {
            this.call();
        }

        private void call() {
            this.latch.countDown();
        }

    }

}

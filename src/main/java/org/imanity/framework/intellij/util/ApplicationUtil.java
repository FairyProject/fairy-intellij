package org.imanity.framework.intellij.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Computable;
import lombok.experimental.UtilityClass;

import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@UtilityClass
public class ApplicationUtil {

    public <T> T invokeAndWait(Supplier<T> supplier) {
        AtomicReference<T> reference = new AtomicReference<>();
        invokeAndWait(() -> reference.set(supplier.get()));
        return reference.get();
    }

    public void invokeAndWait(Runnable runnable) {
        ApplicationManager.getApplication().invokeAndWait(runnable, ModalityState.defaultModalityState());
    }

    public <T> T runWriteTask(Supplier<T> supplier) {
        return invokeAndWait(() -> ApplicationManager.getApplication().runWriteAction((Computable<T>) supplier::get));
    }

    public void runWriteTask(Runnable runnable) {
        invokeAndWait(() -> ApplicationManager.getApplication().runWriteAction((new Computable<Object>() {
            @Override
            public Object compute() {
                runnable.run();
                return null;
            }
        })));
    }

    public void invokeLater(Runnable runnable) {
        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.defaultModalityState());
    }

    public <T> T runReadAction(Supplier<T> supplier) {
        return invokeAndWait(() -> ApplicationManager.getApplication().runReadAction((Computable<T>) supplier::get));
    }

    public void runReadAction(Runnable runnable) {
        invokeAndWait(() -> ApplicationManager.getApplication().runReadAction((new Computable<Object>() {
            @Override
            public Object compute() {
                runnable.run();
                return null;
            }
        })));
    }

}

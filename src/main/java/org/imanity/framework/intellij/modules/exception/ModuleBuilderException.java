package org.imanity.framework.intellij.modules.exception;

public class ModuleBuilderException extends RuntimeException {

    public ModuleBuilderException(String s) {
        super(s);
    }

    public ModuleBuilderException(String s, Throwable ex) {
        super(s, ex);
    }
}

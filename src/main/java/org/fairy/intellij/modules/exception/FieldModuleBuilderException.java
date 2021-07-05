package org.fairy.intellij.modules.exception;

import lombok.Getter;

import javax.swing.*;

@Getter
public class FieldModuleBuilderException extends ModuleBuilderException {

    private final JComponent field;

    public FieldModuleBuilderException(JComponent field, String message) {
        super(message);
        this.field = field;
    }

}

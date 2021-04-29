package org.imanity.framework.intellij.modules.exception;

import lombok.Getter;

import javax.swing.*;

public class EmptyFieldModuleBuilderException extends FieldModuleBuilderException {

    public EmptyFieldModuleBuilderException(JComponent field) {
        super(field, "The field " + field.getName() + " must not be empty!");
    }

}

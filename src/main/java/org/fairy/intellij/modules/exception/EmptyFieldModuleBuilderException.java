package org.fairy.intellij.modules.exception;

import javax.swing.*;

public class EmptyFieldModuleBuilderException extends FieldModuleBuilderException {

    public EmptyFieldModuleBuilderException(JComponent field) {
        super(field, "The field " + field.getName() + " must not be empty!");
    }

}

package org.imanity.framework.intellij.modules.exception;

import lombok.Getter;
import org.apache.xmlbeans.impl.jam.JElement;

import javax.swing.*;

@Getter
public class FieldModuleBuilderException extends ModuleBuilderException {

    private final JComponent field;

    public FieldModuleBuilderException(JComponent field, String message) {
        super(message);
        this.field = field;
    }

}

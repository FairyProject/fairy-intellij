package org.imanity.framework.intellij.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {

    private final String NO_SPACE = "\\S+";

    public boolean isAnySpace(String text) {
        return !text.matches(NO_SPACE);
    }

}

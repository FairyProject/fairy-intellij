package org.fairy.intellij.util;

import java.io.IOException;

public interface ClassContentMapper {

    String apply(String packageName, String className) throws IOException;

}

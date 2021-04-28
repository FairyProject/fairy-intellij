package com.github.leegodsrc.imanityframeworkintellijplugin.services

import com.github.leegodsrc.imanityframeworkintellijplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}

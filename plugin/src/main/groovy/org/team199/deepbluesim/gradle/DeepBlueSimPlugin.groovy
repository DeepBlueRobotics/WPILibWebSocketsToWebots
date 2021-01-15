/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package org.team199.deepbluesim.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

/**
 * A simple 'hello world' plugin.
 */
class DeepBlueSimPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.tasks.register("installDeepBlueSim") {
            doLast {
                def resourceStream = DeepBlueSimPlugin.class.getResourceAsStream("Webots.zip")
                if (resourceStream == null) throw new RuntimeException("resourceStream is null")
                def dbsDir = new File(project.buildDir, "tmp/deepbluesim")
                dbsDir.mkdirs()
                FileUtils.copyInputStreamToFile(resourceStream, new File(dbsDir,"Webots.zip"))
                project.copy {
                    from project.zipTree(new File(dbsDir,"Webots.zip"))
                    into project.projectDir
                }
            }
        }
    }
}
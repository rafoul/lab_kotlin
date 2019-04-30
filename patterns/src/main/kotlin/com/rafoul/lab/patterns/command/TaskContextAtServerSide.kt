package com.rafoul.lab.patterns.command

import java.io.File


fun TaskContext.baseDir(): File = File("/tmp/taskcontext")


fun TaskContext.environmentVariables(): Map<String, String> {
    val baseDir = baseDir()
    return mapOf("BASE_DIR" to baseDir()).mapValues { (_, v) -> v.toRelativeString(baseDir) }
}
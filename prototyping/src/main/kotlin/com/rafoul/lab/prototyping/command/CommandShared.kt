package com.rafoul.lab.prototyping.command

import java.util.*


interface Command {

    val workingDir: String

    val tokens: List<String>

    val environment: Map<String, String>

}



interface ContainerCommand : Command {

    val image: String

    val mounts: Map<String, String>

    val entryPoint: String

}


interface TaskAwareCommand<T: Command> : Command {

    val taskContext: TaskContext

    val command: T

}


data class DefaultCommand(
        override val workingDir: String,
        override val tokens: List<String>,
        override val environment: Map<String, String>
) : Command



data class DefaultContainerCommand(
        private val wrapped: Command,
        override val image: String,
        override val mounts: Map<String, String>,
        override val entryPoint: String
) : ContainerCommand, Command by wrapped {

    override val tokens: List<String>
        get() {
            return LinkedList<String>().also {
                it.addAll(
                        listOf("docker", "run", "--entrypoint=$entryPoint")
                )
                it.add("-w $workingDir")
                environment.forEach {(k, v)->
                    it.add("-e")
                    it.add("$k=$v")
                }
                mounts.forEach { (k, v)->
                    it.add("-v")
                    it.add("$k:$v")
                }
                it.add("$image")
                it.addAll(wrapped.tokens)
            }
        }
}


data class DefaultTaskAwareCommand<T : Command>(
        override val command: T,
        override val taskContext: TaskContext
) : TaskAwareCommand<T>, Command by command
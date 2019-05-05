package com.rafoul.lab.prototyping.command


interface TaskContext : Map<String, Any?> {


}


data class DefaultTaskContext(
        private val delegate: Map<String, Any?>
) : TaskContext, Map<String, Any?> by delegate


interface ShellCommand {

    val workingDir: String

    val tokens: List<String>

    val environment: Map<String, String>

}


interface CommandRequest {

    val command: ShellCommand

}


interface CommandResponse {

    val stdout: String

    val stderr: String

}



interface ContainerCommand : CommandRequest {

    val image: String

    val mounts: Map<String, String>

    val entryPoint: String

}


interface TaskCommand : CommandRequest {

    val taskContext: TaskContext

}


interface TaskAwareContainerCommand : ContainerCommand, TaskCommand


data class ShellCommandImpl(
        override val workingDir: String,
        override val tokens: List<String>,
        override val environment: Map<String, String>
) : ShellCommand


data class TaskAwareLocalShellCommandRequest(
        override val taskContext: TaskContext,
        override val command: ShellCommand
) : TaskCommand


data class TaskAwareContainerCommandRequest(
        override val taskContext: TaskContext,
        override val command: ShellCommand,
        override val image: String,
        override val mounts: Map<String, String>,
        override val entryPoint: String
) : TaskAwareContainerCommand
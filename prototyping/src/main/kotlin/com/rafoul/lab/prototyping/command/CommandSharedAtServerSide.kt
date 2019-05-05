package com.rafoul.lab.prototyping.command


interface CommandExecutor {

    fun execute(cmd: Command)

}


class DefaultExecutor : CommandExecutor {

    override fun execute(cmd: Command) {
        println("${cmd.tokens.joinToString(" ")}")
    }

}


data class TaskAwareContainerCommand(
        override val command: ContainerCommand,
        override val taskContext: TaskContext
) : TaskAwareCommand<ContainerCommand>, ContainerCommand by command {

    override val environment: Map<String, String>
        get() = command.environment + taskContext.environmentVariables()

    override val mounts: Map<String, String>
        get() {
            return command.mounts.toMutableMap().also {
                it[taskContext.baseDir().absolutePath] = workingDir
            }
        }
}
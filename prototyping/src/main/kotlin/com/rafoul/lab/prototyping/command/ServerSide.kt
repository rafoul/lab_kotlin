package com.rafoul.lab.prototyping.command

import java.io.File
import java.util.*


interface CommandRequestHandler {

    fun supports(req: CommandRequest): Int

    fun handles(req: CommandRequest): CommandResponse

}


abstract class LocalShellSupport(private val shell: ShellCommandExecutor) : CommandRequestHandler {

    abstract val conditions: List<(CommandRequest) -> Boolean>

    abstract fun converts(req: CommandRequest): ShellCommand

    override fun supports(req: CommandRequest): Int {
        return conditions.map {
            when (it(req)) {
                true -> 1
                else -> 0
            }
        }.sum()
    }

    override fun handles(req: CommandRequest): CommandResponse {
        val converted = converts(req)
        return shell.execute(converted).let {
            object : CommandResponse {
                override val stdout: String = it.stdout
                override val stderr: String = it.stderr
            }
        }
    }

}


class LocalShellCommandHandler(shell: ShellCommandExecutor) : LocalShellSupport(shell) {

    override val conditions: List<(CommandRequest) -> Boolean> =
            listOf<(CommandRequest) -> Boolean>(
                    { it is TaskCommand }
            )

    override fun converts(req: CommandRequest): ShellCommand {
        require(req is TaskCommand)
        return TaskAwareShellCommand(req)
    }
}


open class TaskAwareShellCommand(private val req: TaskCommand) : ShellCommand by req.command {
    override val workingDir: String = req.command.workingDir.takeUnless { it.isEmpty() } ?: req.taskContext.baseDir().absolutePath
    override val environment: Map<String, String> = req.command.environment + req.taskContext.environmentVariables()
}


class ContainerShellCommand(private val req: TaskAwareContainerCommand) : TaskAwareShellCommand(req) {
    override val tokens: List<String>
        get() {
            return LinkedList<String>().apply {
                addAll(
                        listOf("docker", "run", "--entrypoint=${req.entryPoint}")
                )
                add("-w $workingDir")
                environment.forEach { (k, v) ->
                    add("-e")
                    add("$k=$v")
                }
                req.mounts.toMutableMap().apply {
                    this[req.taskContext.baseDir().absolutePath] = workingDir
                    forEach { (k, v) ->
                        add("-v")
                        add("$k:$v")
                    }
                }
                add("${req.image}")
                addAll(req.command.tokens)
            }
        }
}


class LocalShellContainerCommandHandler(shell: ShellCommandExecutor) : LocalShellSupport(shell) {

    override val conditions: List<(CommandRequest) -> Boolean> =
            listOf<(CommandRequest) -> Boolean>(
                    { it is TaskCommand },
                    { it is ContainerCommand }
            )

    override fun converts(req: CommandRequest): ShellCommand {
        require(req is TaskAwareContainerCommand)
        return ContainerShellCommand(req)
    }
}


fun TaskContext.baseDir(): File = File("/tmp/taskcontext")


fun TaskContext.environmentVariables(): Map<String, String> {
    val baseDir = baseDir()
    return mapOf("BASE_DIR" to baseDir()).mapValues { (_, v) -> v.toRelativeString(baseDir) }
}


interface ShellCommandExecutor {

    fun execute(cmd: ShellCommand): CommandExecutionResult

}


class DefaultShellCommandExecutor : ShellCommandExecutor {

    override fun execute(cmd: ShellCommand): CommandExecutionResult {
        println("${cmd.tokens.joinToString(" ")}")
        return CommandExecutionResult("", "", 0)
    }

}


data class CommandExecutionResult(
        val stdout: String,
        val stderr: String,
        val exitCode: Int
)

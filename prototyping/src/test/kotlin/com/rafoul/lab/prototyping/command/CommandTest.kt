package com.rafoul.lab.prototyping.command

import org.junit.Test
import java.lang.IllegalStateException


class CommandTest {

    private val shell = DefaultShellCommandExecutor()

    private val executors: List<CommandRequestHandler> = listOf(
            LocalShellCommandHandler(shell),
            LocalShellContainerCommandHandler(shell)
    )

    private fun pickup(req: CommandRequest): CommandRequestHandler {
        return executors.maxBy { it.supports(req) }!!
    }

    @Test
    fun testRunCommand() {
        listOf(
                getLocalCommand(),
                getContainerCommand()
        ).forEach {req->
            pickup(req).handles(req)
        }
    }

    private fun getCommand() = ShellCommandImpl(
            "/tmp/helloworld",
            listOf("javac HelloWorld && java HelloWorld"),
            mapOf("SERVER_ADDR" to "localhost")
    )

    private fun getLocalCommand() = TaskAwareLocalShellCommandRequest(
            getTaskContext(),
            getCommand()
    )

    private fun getContainerCommand() = TaskAwareContainerCommandRequest(
            getTaskContext(),
            getCommand().copy(workingDir = "/usr/share/helloworld"),
            "openjdk",
            mapOf("/tmp/helloworld" to "/usr/share/helloworld"),
            ""
    )

    private fun getTaskContext() = DefaultTaskContext(
            mapOf()
    )
}
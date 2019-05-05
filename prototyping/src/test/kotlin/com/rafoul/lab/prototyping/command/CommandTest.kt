package com.rafoul.lab.prototyping.command

import org.junit.Test
import java.lang.IllegalStateException


class CommandTest {

    private val executor = DefaultExecutor()

    @Test
    fun testRunCommand() {
        executor.execute(getCommand())
        executor.execute(getContainerCommand())
    }

    @Test
    fun testRunComplexCommand() {
        val taskAwareCmd = getTaskAwareCommand()
        val unwrapped = if (taskAwareCmd.command is ContainerCommand) {
            TaskAwareContainerCommand(
                    taskAwareCmd.command as ContainerCommand,
                    taskAwareCmd.taskContext
            )
        } else {
            throw IllegalStateException()
        }
        executor.execute(unwrapped)
    }

    private fun getCommand() = DefaultCommand(
            "/tmp/helloworld",
            listOf("javac HelloWorld && java HelloWorld"),
            emptyMap()
    )

    private fun getContainerCommand() = DefaultContainerCommand(
            getCommand().copy(workingDir = "/usr/share/helloworld"),
            "openjdk",
            mapOf("/tmp/helloworld" to "/usr/share/helloworld"),
            ""
    )

    private fun getTaskAwareCommand() = DefaultTaskAwareCommand(
            getContainerCommand(),
            getTaskContext()

    )

    private fun getTaskContext() = DefaultTaskContext(
            mapOf()
    )
}
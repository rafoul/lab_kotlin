package com.rafoul.lab.prototyping.coroutine

import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine


class CoroutineTest {


    @Test
    fun testFoo() {
        val pipeline = PipelineSimplified(
                listOf<suspend (Pipeline)->Unit>(
                        ::doSomethingAfter,
                        ::retrieveContinuation
                )
        )
        Pipeline::proceed.startCoroutine(pipeline,
                Continuation(EmptyCoroutineContext, { println("Execution finished.")})
        )
    }
}
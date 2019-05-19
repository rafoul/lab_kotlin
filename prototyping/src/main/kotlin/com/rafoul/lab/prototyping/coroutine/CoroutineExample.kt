package com.rafoul.lab.prototyping.coroutine

import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn


interface Pipeline {

    suspend fun proceed()

}


class DefaultPipeline(
        val interceptors: List<PipelineInteceptor>
) : Pipeline {

    private var index = 0

    override suspend fun proceed() {
        while (index < interceptors.size) {
            val index = index++
            interceptors[index](this)
        }
    }

}


class PipelineSimplified(
        val interceptors: List<PipelineInteceptor>
) : Pipeline {

    private var index = 0

    override suspend fun proceed() {
        suspendCoroutineUninterceptedOrReturn<Unit> { c ->
            if (!loop(c)) {
                COROUTINE_SUSPENDED
            }
        }
    }

    private fun loop(c: Continuation<Unit>): Boolean {
        while (index < interceptors.size) {
            val index = index++
            val rtn = (interceptors[index] as Function2<Pipeline, Continuation<Unit>, Any?>).invoke(this, c)
            if (rtn == COROUTINE_SUSPENDED) {
                return false
            }
        }
        return true
    }

}



typealias PipelineInteceptor = suspend (Pipeline) -> Unit


suspend fun doSomethingAfter(pipeline: Pipeline) {
    println("doSomethingAfter start.")
    pipeline.proceed()
    println("doSomethingAfter finishes.")
}


suspend fun retrieveContinuation(pipeline: Pipeline) {
    suspendCoroutineUninterceptedOrReturn<Unit> { c ->
        println(c)
    }
}
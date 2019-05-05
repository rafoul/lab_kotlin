package com.rafoul.lab.prototyping.command


interface TaskContext : Map<String, Any?> {


}


data class DefaultTaskContext(
        private val delegate: Map<String, Any?>
) : TaskContext, Map<String, Any?> by delegate
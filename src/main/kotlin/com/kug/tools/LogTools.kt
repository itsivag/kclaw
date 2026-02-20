package com.kug.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

class LogTools : ToolSet {

    private val _messages = mutableListOf<String>()

    /** Everything the agent logged during this run, in order. */
    val accumulated: String get() = _messages.joinToString("\n\n")

    @Tool
    @LLMDescription("Log a message to standard output.")
    fun log(message: String): String {
        println(message)
        _messages.add(message)
        return "Logged."
    }
}
package com.kug.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

class LogTools : ToolSet {

    @Tool
    @LLMDescription("Log a message to standard output.")
    fun log(message: String): String {
        println(message)
        return "Logged."
    }
}
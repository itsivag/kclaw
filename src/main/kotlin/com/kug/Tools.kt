package com.kug

import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.reflect.ToolSet

class CliTools : ToolSet {

    @Tool
    @LLMDescription("Asks the user for input with a prompt string and returns their response")
    fun askUser(prompt: String): String {
        print("$prompt ")
        return readlnOrNull().orEmpty()
    }

    @Tool
    @LLMDescription("Prints a message to the user")
    fun sayToUser(message: String): String {
        println(message)
        return "OK"
    }
}
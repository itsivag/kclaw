package com.kug.tools

import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CliTools : ToolSet {

    @Tool
    @LLMDescription("Send a message to the user and wait for their reply. Always use this tool to communicate.")
    suspend fun chat(message: String): String {
        println(message)
        return withContext(Dispatchers.IO) {
            readlnOrNull() ?: ""
        }
    }
}
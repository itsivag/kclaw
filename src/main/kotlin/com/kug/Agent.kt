package com.kug

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Agent {
    suspend fun runAgent()
}

class AgentImpl : Agent {
    val apiKey = System.getenv("GOOGLE_API_KEY") ?: error("The API key is not set.")

    val toolRegistry = ToolRegistry {
        tools(CliTools().asTools())
    }

    val agent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        llmModel = GoogleModels.Gemini2_5Pro,
        systemPrompt = "You are a helpful assistant. Always use SayToUser to respond to the user and AskUser to get their input. Keep the conversation going until the user says goodbye.",
        strategy = chatAgentStrategy(),
        toolRegistry = toolRegistry
    )

    override suspend fun runAgent() {
        agent.run("Start the conversation by greeting the user.")
    }
}
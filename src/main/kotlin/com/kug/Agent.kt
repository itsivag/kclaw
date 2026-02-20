package com.kug

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Agent {
    suspend fun runAgent(): String
}

class AgentImpl : Agent {
    val apiKey = System.getenv("GOOGLE_API_KEY")
        ?: error("The API key is not set.")

    // Create an agent
    val agent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        llmModel = GoogleModels.Gemini2_5Pro
    )

    override suspend fun runAgent(): String {
        return withContext(Dispatchers.IO) {
            agent.run("Hello, World!")
        }
    }
}
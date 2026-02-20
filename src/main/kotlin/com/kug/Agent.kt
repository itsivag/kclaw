package com.kug

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import java.io.File

interface Agent {
    suspend fun runAgent()
}

class AgentImpl : Agent {
    val apiKey = System.getenv("GOOGLE_API_KEY") ?: error("The API key is not set.")

    val toolRegistry = ToolRegistry {
        tools(CliTools().asTools())
        tools(FileTools().asTools())
    }

    val agent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        llmModel = GoogleModels.Gemini2_5Pro,
        systemPrompt = """
            You have full access to your install directory via the file tools.
            All file paths are relative to your install directory.
            Use the chat tool to send a message and get the user's reply. Always use chat — never respond with plain text.
            Continue the conversation until the user says goodbye.
        """.trimIndent(),
        strategy = chatAgentStrategy(),
        toolRegistry = toolRegistry
    )

    override suspend fun runAgent() {
        val kclawDir = File(installDir(), ".kclaw")
        val agentMd = File(kclawDir, "AGENT.md").takeIf { it.exists() }?.readText()
        if (agentMd == null) {
            println("Agent is not configured. Please run 'kclaw onboard' to set up.")
            return
        }
        val identityMd = File(kclawDir, "IDENTITY.md").takeIf { it.exists() }?.readText() ?: "(not set)"
        val heartbeatMd = File(kclawDir, "HEARTBEAT.md").takeIf { it.exists() }?.readText() ?: "(not set)"
        val memoryMd = File(kclawDir, "MEMORY.md").takeIf { it.exists() }?.readText() ?: "(empty)"
        agent.run(
            """
            AGENT.md:
            $agentMd

            IDENTITY.md:
            $identityMd

            HEARTBEAT.md:
            $heartbeatMd

            MEMORY.md (your long-term memory from previous sessions):
            $memoryMd

            Follow the instructions above and greet the user.
            """.trimIndent()
        )
    }
}
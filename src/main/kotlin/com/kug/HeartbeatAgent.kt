package com.kug

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import java.io.File

interface HeartbeatAgent {
    suspend fun runHeartbeat()
}

class HeartbeatAgentImpl : HeartbeatAgent {
    private val apiKey = System.getenv("GOOGLE_API_KEY") ?: error("The API key is not set.")

    private val agent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        llmModel = GoogleModels.Gemini2_5Pro,
        systemPrompt = """
            You are a headless background agent with no user present.
            Use the log tool to record your actions and observations.
            When you are done, respond with a plain text summary (no tool call).
        """.trimIndent(),
        strategy = reActStrategy(),
        toolRegistry = ToolRegistry {
            tools(FileTools().asTools())
            tools(LogTools().asTools())
        }
    )

    override suspend fun runHeartbeat() {
        val kclawDir = File(installDir(), ".kclaw")
        val heartbeatMd = File(kclawDir, "HEARTBEAT.md").takeIf { it.exists() }?.readText()
            ?: run { println("HEARTBEAT.md not found. Run 'kclaw onboard' first."); return }

        agent.run(
            """
            IDENTITY.md: ${File(kclawDir, "IDENTITY.md").takeIf { it.exists() }?.readText() ?: "(not set)"}
            HEARTBEAT.md: $heartbeatMd
            MEMORY.md: ${File(kclawDir, "MEMORY.md").takeIf { it.exists() }?.readText() ?: "(empty)"}
            Execute your heartbeat instructions. Log actions. Finish with a plain text summary.
            """.trimIndent()
        )
    }
}

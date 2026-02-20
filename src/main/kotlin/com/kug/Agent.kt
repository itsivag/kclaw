package com.kug

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import java.io.File

interface Agent {
    suspend fun runAgent(args: String? = null)
}

class AgentImpl : Agent {
    val apiKey = System.getenv("GOOGLE_API_KEY") ?: error("The API key is not set.")

    val toolRegistry = ToolRegistry {
        tools(CliTools().asTools())
        tools(FileTools().asTools())
        tools(CronTools().asTools())
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

    // Single-run agent used when --args is provided (e.g. cron reminders).
    // Uses reActStrategy so it exits as soon as it emits plain text.
    val singleRunAgent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        llmModel = GoogleModels.Gemini2_5Pro,
        systemPrompt = """
            You are a headless agent delivering a message.
            Use the log tool to print the message to the user, then respond with a plain text confirmation to finish.
        """.trimIndent(),
        strategy = reActStrategy(),
        toolRegistry = ToolRegistry {
            tools(FileTools().asTools())
            tools(LogTools().asTools())
        }
    )

    override suspend fun runAgent(args: String?) {
        val kclawDir = File(installDir(), ".kclaw")
        val identityMd = File(kclawDir, "IDENTITY.md").takeIf { it.exists() }?.readText() ?: "(not set)"
        val memoryMd = File(kclawDir, "MEMORY.md").takeIf { it.exists() }?.readText() ?: "(empty)"

        if (args != null) {
            singleRunAgent.run("""
                IDENTITY.md: $identityMd
                MEMORY.md: $memoryMd
                Message: $args
                Relay this message to the user using the log tool, then finish.
            """.trimIndent())
            return
        }

        val agentMd = File(kclawDir, "AGENT.md").takeIf { it.exists() }?.readText()
        if (agentMd == null) {
            println("Agent is not configured. Please run 'kclaw onboard' to set up.")
            return
        }
        val heartbeatMd = File(kclawDir, "HEARTBEAT.md").takeIf { it.exists() }?.readText() ?: "(not set)"
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
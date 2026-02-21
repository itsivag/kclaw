package com.kug.sol

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.kug.tools.CliTools
import com.kug.tools.CronTools
import com.kug.tools.CryptoTools
import com.kug.tools.FileTools
import com.kug.tools.LogTools
import com.kug.tools.WebTools
import com.kug.tools.appendCronLog
import com.kug.utils.installDir
import java.io.File

interface Agent {
    suspend fun runAgent(args: String? = null, label: String? = null)
}

class AgentImpl : Agent {
    private val apiKey = System.getenv("GOOGLE_API_KEY") ?: error("GOOGLE_API_KEY is not set.")

    private val toolRegistry = ToolRegistry {
        tools(CliTools().asTools())
        tools(FileTools().asTools())
        tools(CronTools().asTools())
        tools(WebTools().asTools())
        tools(CryptoTools().asTools())
    }

    private val agent = AIAgent(

        promptExecutor = simpleGoogleAIExecutor(apiKey),
        llmModel = GoogleModels.Gemini2_5Pro,
        systemPrompt = """
            You have full access to your install directory via the file tools.
            All file paths are relative to your install directory.
            Use the chat tool to send a message and get the user's reply. Always use chat — never respond with plain text.
            Continue the conversation until the user says goodbye.
        """.trimIndent(),
        strategy = chatAgentStrategy(),
        toolRegistry = toolRegistry,
        maxIterations = 500
    )

    override suspend fun runAgent(args: String?, label: String?) {
        val kclawDir = File(installDir(), ".kclaw")
        val identityMd = File(kclawDir, "IDENTITY.md").takeIf { it.exists() }?.readText() ?: "(not set)"
        val memoryMd = File(kclawDir, "MEMORY.md").takeIf { it.exists() }?.readText() ?: "(empty)"

        if (args != null) {
            val logLabel = label ?: args.substringBefore(":").trim().lowercase().replace(" ", "-")
            val tmpPath = ".kclaw/logs/.$logLabel.tmp.md"

            val taskAgent = AIAgent(
                promptExecutor = simpleGoogleAIExecutor(apiKey),
                llmModel = GoogleModels.Gemini2_5Pro,
                systemPrompt = """
                    You are a headless background agent executing a scheduled task. No user is present.
                    Use your tools to complete the task fully — search the web, read and write files, etc.
                    When you have gathered all findings, write the complete results to the file specified in the task using writeFile.
                    Then finish with a plain text completion message.
                """.trimIndent(),
                strategy = reActStrategy(),
                toolRegistry = ToolRegistry {
                    tools(FileTools().asTools())
                    tools(WebTools().asTools())
                    tools(CronTools().asTools())
                },
                maxIterations = 500
            )
            taskAgent.run(
                """
                IDENTITY.md: $identityMd
                MEMORY.md: $memoryMd
                Task: $args
                Results file: $tmpPath
                Perform the task, then write ALL findings (titles, summaries, scores, links, tool names, etc.) to the Results file using writeFile. Then finish.
                """.trimIndent()
            )

            val tmpFile = File(installDir(), tmpPath)
            val content = tmpFile.takeIf { it.exists() }?.readText() ?: "(agent produced no output)"
            tmpFile.delete()
            appendCronLog(logLabel, args, content)
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

package com.kug.sol

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.kug.tools.CronTools
import com.kug.tools.FileTools
import com.kug.tools.appendCronLog
import com.kug.utils.installDir
import java.io.File

interface HeartbeatAgent {
    suspend fun runHeartbeat()
}

class HeartbeatAgentImpl : HeartbeatAgent {
    private val apiKey = System.getenv("GOOGLE_API_KEY") ?: error("The API key is not set.")

    override suspend fun runHeartbeat() {
        val kclawDir = File(installDir(), ".kclaw")
        val heartbeatMd = File(kclawDir, "HEARTBEAT.md").takeIf { it.exists() }?.readText()
            ?: run { println("HEARTBEAT.md not found. Run 'kclaw onboard' first."); return }

        val tmpPath = ".kclaw/logs/.heartbeat.tmp.md"
        val agent = AIAgent(
            promptExecutor = simpleGoogleAIExecutor(apiKey),
            llmModel = GoogleModels.Gemini2_5Pro,
            systemPrompt = """
                You are a headless background agent with no user present.
                Use your tools to complete your instructions fully.
                When you have gathered all findings, write the complete results to the file specified in the task using writeFile.
                Then finish with a plain text completion message.
            """.trimIndent(),
            strategy = reActStrategy(),
            toolRegistry = ToolRegistry {
                tools(FileTools().asTools())
                tools(CronTools().asTools())
            }
        )

        agent.run(
            """
            IDENTITY.md: ${File(kclawDir, "IDENTITY.md").takeIf { it.exists() }?.readText() ?: "(not set)"}
            HEARTBEAT.md: $heartbeatMd
            MEMORY.md: ${File(kclawDir, "MEMORY.md").takeIf { it.exists() }?.readText() ?: "(empty)"}
            Results file: $tmpPath
            Execute your heartbeat instructions, then write ALL findings to the Results file using writeFile. Then finish.
            """.trimIndent()
        )

        val tmpFile = File(installDir(), tmpPath)
        val content = tmpFile.takeIf { it.exists() }?.readText() ?: "(agent produced no output)"
        tmpFile.delete()
        appendCronLog("heartbeat", "Heartbeat", content)
    }
}

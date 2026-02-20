package com.kug

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.runBlocking
import java.io.File

class Kclaw : CliktCommand(name = "kclaw") {
    override fun run() = Unit
}

class Start(val agent: Agent) : CliktCommand(name = "start") {
    override fun help(context: Context) = "start the agent"
    val args by option("--args", help = "message to pass to the agent instead of starting an interactive session")
    val label by option("--label", help = "log file label for this scheduled run")

    override fun run() {
        runBlocking {
            agent.runAgent(args, label)
        }
    }
}

class Onboard : CliktCommand(name = "onboard") {
    override fun help(context: Context): String {
        return "create .kclaw/IDENTITY.md, .kclaw/AGENT.md, and .kclaw/HEARTBEAT.md in the install directory"
    }

    override fun run() {
        val kclawDir = File(installDir(), ".kclaw")
        if (!kclawDir.exists()) {
            kclawDir.mkdirs()
            echo("Created ${kclawDir.path}")
        }

        val files = mapOf(
            "IDENTITY.md" to IDENTITY_MD,
            "AGENT.md" to AGENT_MD,
            "HEARTBEAT.md" to HEARTBEAT_MD,
            "MEMORY.md" to MEMORY_MD,
        )
        for ((name, content) in files) {
            val file = File(kclawDir, name)
            if (file.exists()) {
                echo("$name already exists, skipping.")
            } else {
                file.writeText(content)
                echo("Created $name")
            }
        }

        registerHeartbeatCron()
    }

    private fun registerHeartbeatCron() {
        val binary = File(installDir(), "bin/kclaw").absolutePath
        val cronLine = "*/15 * * * * $binary heartbeat"

        val listResult = ProcessBuilder("crontab", "-l")
            .redirectErrorStream(true)
            .start()
        val existingCrontab = listResult.inputStream.bufferedReader().readText()
        listResult.waitFor()
        // Treat "no crontab" messages as an empty crontab
        val currentLines = if (existingCrontab.contains("no crontab for")) {
            emptyList()
        } else {
            existingCrontab.lines()
        }

        val newLines = if (currentLines.any { it.contains("kclaw heartbeat") }) {
            currentLines.map { if (it.contains("kclaw heartbeat")) cronLine else it }
        } else {
            currentLines + cronLine
        }

        val newCrontab = newLines.joinToString("\n").trimEnd() + "\n"

        val writeProc = ProcessBuilder("crontab", "-")
            .redirectErrorStream(true)
            .start()
        writeProc.outputStream.bufferedWriter().use { it.write(newCrontab) }
        writeProc.waitFor()

        echo("Registered heartbeat cron: $cronLine")
    }
}

class Heartbeat(val heartbeatAgent: HeartbeatAgent) : CliktCommand(name = "heartbeat") {
    override fun help(context: Context) = "run the headless heartbeat agent (called from cron)"
    override fun run() = runBlocking { heartbeatAgent.runHeartbeat() }
}

class Crons : CliktCommand(name = "crons") {
    override fun help(context: Context) = "list all cron jobs registered by the agent"

    override fun run() {
        val proc = ProcessBuilder("crontab", "-l")
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().readText()
        proc.waitFor()

        if (output.contains("no crontab for")) {
            echo("No cron jobs found.")
            return
        }

        val managed = output.lines().filter { it.contains("# kclaw:") }
        if (managed.isEmpty()) {
            echo("No agent-managed cron jobs found.")
            return
        }

        managed.forEach { line ->
            val label = line.substringAfter("# kclaw:").trim()
            val schedule = line.trim().split(" ").take(5).joinToString(" ")
            echo("[$label]  $schedule  …")
            echo("  $line")
        }
    }
}

fun main(args: Array<String>) {
    val agent = AgentImpl()
    val heartbeatAgent = HeartbeatAgentImpl()
    Kclaw().subcommands(Start(agent), Onboard(), Heartbeat(heartbeatAgent), Crons()).main(args)
}

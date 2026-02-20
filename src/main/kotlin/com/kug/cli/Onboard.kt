package com.kug.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.kug.utils.AGENT_MD
import com.kug.utils.HEARTBEAT_MD
import com.kug.utils.IDENTITY_MD
import com.kug.utils.MEMORY_MD
import com.kug.utils.installDir
import java.io.File

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
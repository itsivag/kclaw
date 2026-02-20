package com.kug.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

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
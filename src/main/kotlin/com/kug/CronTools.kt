package com.kug

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import java.io.File

class CronTools : ToolSet {

    // ── crontab helpers ───────────────────────────────────────────────────────

    private fun readCrontab(): List<String> {
        val proc = ProcessBuilder("crontab", "-l")
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().readText()
        proc.waitFor()
        return if (output.contains("no crontab for")) emptyList()
        else output.lines().dropLastWhile { it.isBlank() }
    }

    private fun writeCrontab(lines: List<String>) {
        val content = lines.joinToString("\n").trimEnd() + "\n"
        val proc = ProcessBuilder("crontab", "-")
            .redirectErrorStream(true)
            .start()
        proc.outputStream.bufferedWriter().use { it.write(content) }
        proc.waitFor()
    }

    private fun upsert(cronLine: String, label: String): String {
        val current = readCrontab()
        val tag = "# kclaw:$label"
        val updated = if (current.any { it.contains(tag) }) {
            current.map { if (it.contains(tag)) "$cronLine $tag" else it }
        } else {
            current + "$cronLine $tag"
        }
        writeCrontab(updated)
        return "$cronLine $tag"
    }

    private val binary get() = File(installDir(), "bin/kclaw").absolutePath

    // Escape characters that are special inside a double-quoted cron argument.
    private fun shellQuote(text: String) = "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\\$").replace("`", "\\`") + "\""

    // ── tools ─────────────────────────────────────────────────────────────────

    @Tool
    @LLMDescription(
        "Schedule a recurring agent task. " +
        "'schedule' is a standard 5-field cron expression (e.g. '*/5 * * * *' = every 5 min, '0 9 * * 1-5' = 9 am weekdays). " +
        "'task' is a natural language instruction for the agent (e.g. 'search for top Reddit posts and save findings'). " +
        "'label' is a unique short name used to identify or replace this job later. Results are saved to .kclaw/logs/<label>.md."
    )
    fun setCronJob(schedule: String, task: String, label: String): String {
        val command = "$binary start --args ${shellQuote(task)} --label ${shellQuote(label)}"
        val line = upsert("$schedule $command", label)
        return "Scheduled: $line"
    }

    @Tool
    @LLMDescription(
        "List kclaw-managed cron jobs. " +
        "Pass an empty string for 'filter' to list all, or a label substring to narrow results."
    )
    fun listCronJobs(filter: String): String {
        val all = readCrontab().filter { it.contains("# kclaw:") }
        val matched = if (filter.isBlank()) all else all.filter { it.contains(filter) }
        return if (matched.isEmpty()) "No matching cron jobs found." else matched.joinToString("\n")
    }

    @Tool
    @LLMDescription("Remove a kclaw-managed cron job by its label.")
    fun removeCronJob(label: String): String {
        val current = readCrontab()
        val tag = "# kclaw:$label"
        val updated = current.filter { !it.contains(tag) }
        if (updated.size == current.size) return "No cron job found with label '$label'."
        writeCrontab(updated)
        return "Removed cron job '$label'."
    }

    @Tool
    @LLMDescription(
        "Set a reminder. At the scheduled time, kclaw start --args is called and the agent prints the message. " +
        "'schedule' is a cron expression (e.g. '30 8 * * *' = 8:30 am every day, '0 17 * * 5' = 5 pm every Friday). " +
        "'message' is the reminder text. " +
        "'label' is a unique name for this reminder."
    )
    fun setReminder(schedule: String, message: String, label: String): String {
        val command = "$binary start --args ${shellQuote("REMINDER: $message")} --label ${shellQuote(label)}"
        val line = upsert("$schedule $command", label)
        return "Reminder set: $line"
    }

    @Tool
    @LLMDescription(
        "Set an alarm. At the scheduled time, kclaw start --args is called and the agent prints the alarm message. " +
        "'schedule' is a cron expression (e.g. '0 7 * * *' = 7 am every day). " +
        "'message' is the alarm text. " +
        "'label' is a unique name for this alarm."
    )
    fun setAlarm(schedule: String, message: String, label: String): String {
        val command = "$binary start --args ${shellQuote("ALARM: $message")} --label ${shellQuote(label)}"
        val line = upsert("$schedule $command", label)
        return "Alarm set: $line"
    }
}

package com.kug

import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun appendCronLog(label: String, title: String, content: String) {
    val logDir = File(installDir(), ".kclaw/logs")
    logDir.mkdirs()
    val logFile = File(logDir, "$label.md")
    val lockFile = File(logDir, "$label.lock")
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    val entry = buildString {
        appendLine("## $timestamp — $title")
        appendLine()
        appendLine(content.trim())
        appendLine()
        appendLine("---")
        appendLine()
    }
    // Cross-process file lock so concurrent kclaw processes don't corrupt the log.
    RandomAccessFile(lockFile, "rw").use { raf ->
        var lock: FileLock? = null
        try {
            lock = raf.channel.lock()
            logFile.appendText(entry)
        } finally {
            lock?.release()
        }
    }
}

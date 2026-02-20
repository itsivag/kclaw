package com.kug.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.kug.sol.HeartbeatAgent
import kotlinx.coroutines.runBlocking

class Heartbeat(val heartbeatAgent: HeartbeatAgent) : CliktCommand(name = "heartbeat") {
    override fun help(context: Context) = "run the headless heartbeat agent (called from cron)"
    override fun run() = runBlocking { heartbeatAgent.runHeartbeat() }
}
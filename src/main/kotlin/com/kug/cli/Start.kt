package com.kug.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.option
import com.kug.sol.Agent
import kotlinx.coroutines.runBlocking

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
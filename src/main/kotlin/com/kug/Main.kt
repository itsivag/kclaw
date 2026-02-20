package com.kug

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.main
import kotlinx.coroutines.runBlocking

class Kclaw : CliktCommand(name = "kclaw") {
    override fun run() = Unit
}

class Start(val agent: Agent) : CliktCommand(name = "start") {
    override fun help(context: Context): String {
        return "start the agent"
    }

    override fun run() {
        runBlocking {
            agent.runAgent()
        }
    }
}

class Onboard : CliktCommand(name = "onboard") {
    override fun help(context: Context): String {
        return "create .kclaw/IDENTITY.md, .kclaw/AGENT.md, and .kclaw/HEARTBEAT.md in the install directory"
    }

    override fun run() {
        val kclawDir = java.io.File(installDir(), ".kclaw")
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
            val file = java.io.File(kclawDir, name)
            if (file.exists()) {
                echo("$name already exists, skipping.")
            } else {
                file.writeText(content)
                echo("Created $name")
            }
        }
    }
}

fun main(args: Array<String>) {
    val agent = AgentImpl()
    Kclaw().subcommands(Start(agent), Onboard()).main(args)
}

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
            val res = agent.runAgent()
            echo(res)
        }
    }
}

fun main(args: Array<String>) {
    val agent = AgentImpl()
    Kclaw().subcommands(Start(agent)).main(args)
}

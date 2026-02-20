package com.kug

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.kug.cli.Crons
import com.kug.cli.Heartbeat
import com.kug.cli.Onboard
import com.kug.cli.Start
import com.kug.sol.Agent
import com.kug.sol.AgentImpl
import com.kug.sol.HeartbeatAgent
import com.kug.sol.HeartbeatAgentImpl
import com.kug.utils.AGENT_MD
import com.kug.utils.HEARTBEAT_MD
import com.kug.utils.IDENTITY_MD
import com.kug.utils.MEMORY_MD
import com.kug.utils.installDir
import kotlinx.coroutines.runBlocking
import java.io.File

class Kclaw : CliktCommand(name = "kclaw") {
    override fun run() = Unit
}

fun main(args: Array<String>) {
    val agent = AgentImpl()
    val heartbeatAgent = HeartbeatAgentImpl()
    Kclaw().subcommands(Start(agent), Onboard(), Heartbeat(heartbeatAgent), Crons()).main(args)
}

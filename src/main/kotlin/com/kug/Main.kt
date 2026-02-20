package com.kug

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.main

class Kclaw : CliktCommand(name = "kclaw") {
    override fun run() = Unit
}

class HelloWorld : CliktCommand(name = "helloworld") {
    override fun help(context: Context) = "Prints Hello World"
    override fun run() {
        echo("Hello, World!")
    }
}

fun main(args: Array<String>) {
    Kclaw().subcommands(HelloWorld()).main(args)
}

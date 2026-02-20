package com.kug

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CliIO {
    suspend fun ask(prompt: String): String
    suspend fun say(message: String)
}


class TerminalCliIO : CliIO {
    override suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        print("$prompt ")
        readlnOrNull() ?: ""
    }

    override suspend fun say(message: String) = withContext(Dispatchers.IO) {
        println(message)
    }
}
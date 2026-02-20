package com.kug.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class WebTools : ToolSet {

    private val tavilyKey = System.getenv("TAVILY_API_KEY") ?: ""
    private val http = HttpClient.newHttpClient()

    @Tool
    @LLMDescription(
        "Search the web and return an AI-synthesized answer with source URLs. " +
        "Requires TAVILY_API_KEY environment variable."
    )
    fun searchWeb(query: String): String {
        if (tavilyKey.isBlank()) return "TAVILY_API_KEY is not set."
        val safeQuery = query.replace("\\", "\\\\").replace("\"", "\\\"")
        val body = """{"query":"$safeQuery","search_depth":"basic","include_answer":true,"max_results":5}"""
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tavily.com/search"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $tavilyKey")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()
            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) response.body()
            else "Error: HTTP ${response.statusCode()} — ${response.body()}"
        } catch (e: Exception) {
            "Error searching: ${e.message}"
        }
    }
}
package com.kug.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CryptoTools : ToolSet {

    private val http = HttpClient.newHttpClient()

    @Tool
    @LLMDescription(
        "Fetch current cryptocurrency market prices in USD from CoinGecko. " +
        "Optionally filter by a comma-separated list of coin IDs (e.g. 'bitcoin,ethereum,solana'). " +
        "Leave 'coinIds' blank to get the top 100 coins by market cap. " +
        "Returns JSON with price, market cap, 24h volume, and 24h price change."
    )
    fun getCryptoPrices(coinIds: String): String {
        val base = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=false"
        val url = if (coinIds.isBlank()) base else "$base&ids=${coinIds.trim()}"
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build()
            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) response.body()
            else "Error: HTTP ${response.statusCode()} — ${response.body()}"
        } catch (e: Exception) {
            "Error fetching crypto prices: ${e.message}"
        }
    }
}

package com.raptoreum.report

import com.raptoreum.report.BuildConfig.AI_AGENT_KEY
import com.raptoreum.report.BuildConfig.AI_AGENT_MODEL
import com.raptoreum.report.BuildConfig.AI_AGENT_URL
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class InsightAgent {

    fun buildNarrative(payload: ReportPayload): String {
        val fallback = fallbackNarrative(payload)
        if (AI_AGENT_URL.isBlank() || AI_AGENT_KEY.isBlank()) {
            return fallback
        }

        return runCatching { requestAiNarrative(payload) }
            .getOrElse { throwable ->
                "$fallback\n\nAI agent fallback: ${throwable.localizedMessage}"
            }
    }

    private fun requestAiNarrative(payload: ReportPayload): String {
        val prompt = buildPrompt(payload)
        val connection = (URL(AI_AGENT_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $AI_AGENT_KEY")
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 20_000
        }

        val body = JSONObject().apply {
            put("model", if (AI_AGENT_MODEL.isNotBlank()) AI_AGENT_MODEL else "free-tier-model")
            put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", systemPrompt()))
                    .put(JSONObject().put("role", "user").put("content", prompt))
            )
            put("max_tokens", 400)
            put("temperature", 0.6)
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body.toString())
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            val error = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
            throw IllegalStateException("AI agent call failed ($responseCode): ${error ?: "no error body"}")
        }

        val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
        val content = JSONObject(responseText)
            .optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
        return content?.trim().takeUnless { it.isNullOrBlank() }
            ?: "AI agent returned an empty response."
    }

    private fun buildPrompt(payload: ReportPayload): String {
        val summary = payload.sections.joinToString(separator = "\n\n") { section ->
            val joined = section.highlights.take(4).joinToString(separator = "\n")
            "${section.title} (${section.url}):\n$joined"
        }
        return """
            Turn these Raptoreum highlights into a concise, energetic briefing. Be specific, cite products or stats you see, and close with the most urgent calls-to-action for the ecosystem. Use short paragraphs and bullets.
            
            $summary
        """.trimIndent()
    }

    private fun fallbackNarrative(payload: ReportPayload): String {
        val topLines = payload.sections.mapNotNull { section ->
            val firstLine = section.highlights.firstOrNull()
            firstLine?.let { "• ${section.title}: $it" }
        }
        return buildString {
            appendLine("AI Insights (offline fallback)")
            appendLine("The agent will auto-run when a free API endpoint is configured.")
            topLines.takeIf { it.isNotEmpty() }?.forEach { appendLine(it) }
        }.trim()
    }

    private fun systemPrompt(): String =
        "You are a crypto research analyst specializing in the Raptoreum ecosystem. Extract outcomes, momentum, and actionable insights, and rewrite them in crisp, newsworthy language with light enthusiasm."
}

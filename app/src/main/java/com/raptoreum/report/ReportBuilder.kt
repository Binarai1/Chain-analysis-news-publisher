package com.raptoreum.report

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReportBuilder(
    private val insightAgent: InsightAgent = InsightAgent()
) {
    fun buildReport(payload: ReportPayload): String {
        val header = """
            Raptoreum Ecosystem Intelligence Report
            Generated: ${payload.metadata.generatedAt}
            Sources scanned: ${payload.metadata.sources.joinToString()}
        """.trimIndent()

        val aiNarrative = insightAgent.buildNarrative(payload)
        val aiSection = """
            AI Insights
            $aiNarrative
        """.trimIndent()

        val body = payload.sections.joinToString("\n\n") { section ->
            val highlights = section.highlights
                .takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "- ", separator = "\n- ")
                ?: "No highlights captured."
            """
                ${section.title}
                Link: ${section.url}
                $highlights
            """.trimIndent()
        }

        val footer = """
            Report ID: ${reportId()}
            Tip: Tap the share icons to send this PDF to your favorite channel.
        """.trimIndent()

        return listOf(header, aiSection, body, footer).joinToString("\n\n")
    }

    private fun reportId(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return "RTM-${LocalDate.now().format(formatter)}"
    }
}

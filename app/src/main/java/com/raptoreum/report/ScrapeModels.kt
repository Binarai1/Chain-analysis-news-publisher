package com.raptoreum.report

data class ScrapeTarget(
    val name: String,
    val url: String,
    val description: String,
    val cssSelectors: List<String>
)

data class ScrapedSection(
    val title: String,
    val highlights: List<String>,
    val url: String
)

data class ReportMetadata(
    val generatedAt: String,
    val sources: List<String>
)

data class ReportPayload(
    val metadata: ReportMetadata,
    val sections: List<ScrapedSection>
)

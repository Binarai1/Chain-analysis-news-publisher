package com.raptoreum.report

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ScraperRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val defaultTargets = listOf(
        ScrapeTarget(
            name = "Raptoreum Website",
            url = "https://raptoreum.com/",
            description = "Official site for ecosystem announcements and docs",
            cssSelectors = listOf("h1", "h2", "article", "section")
        ),
        ScrapeTarget(
            name = "Medium Blog",
            url = "https://medium.com/@raptoreum",
            description = "Community blog posts and development diaries",
            cssSelectors = listOf("h1", "h2", "h3", "article")
        ),
        ScrapeTarget(
            name = "Reddit",
            url = "https://www.reddit.com/r/Raptoreum/",
            description = "Community sentiment and daily chatter",
            cssSelectors = listOf("h1", "h2", "h3", "a[href*=comments]")
        ),
        ScrapeTarget(
            name = "Twitter / X",
            url = "https://x.com/raptoreum",
            description = "Latest social posts and engagement",
            cssSelectors = listOf("article", "div[role=article]", "span")
        ),
        ScrapeTarget(
            name = "YouTube",
            url = "https://www.youtube.com/@RaptoreumOfficial/videos",
            description = "Newest video drops and captions",
            cssSelectors = listOf("h3", "a#video-title", "yt-formatted-string")
        ),
        ScrapeTarget(
            name = "CoinGecko",
            url = "https://www.coingecko.com/en/coins/raptoreum",
            description = "Market cap, price, and volume movements",
            cssSelectors = listOf("h1", "h2", "span", "table")
        ),
        ScrapeTarget(
            name = "CoinMarketCap",
            url = "https://coinmarketcap.com/currencies/raptoreum/",
            description = "Market metrics and social metrics",
            cssSelectors = listOf("h1", "h2", "div", "p")
        )
    )

    suspend fun scrapeAll(
        updateStatus: (String) -> Unit,
        customTargets: List<ScrapeTarget> = emptyList()
    ): ReportPayload = coroutineScope {
        val targets = (customTargets + defaultTargets).distinctBy { it.url }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val metadata = ReportMetadata(
            generatedAt = LocalDateTime.now().format(formatter),
            sources = targets.map { it.url }
        )

        val sections = targets.map { target ->
            async(dispatcher) {
                updateStatus("Scraping ${target.name} …")
                val highlights = scrapeTarget(target)
                updateStatus("Captured ${highlights.size} highlights from ${target.name}")
                ScrapedSection(
                    title = target.name,
                    highlights = highlights,
                    url = target.url
                )
            }
        }.awaitAll()

        ReportPayload(metadata = metadata, sections = sections)
    }

    private fun scrapeTarget(target: ScrapeTarget): List<String> {
        return runCatching {
            val document = Jsoup.connect(target.url)
                .userAgent("Mozilla/5.0 (Android)")
                .timeout(15_000)
                .get()
            extractHighlights(document, target.cssSelectors)
        }.getOrElse { throwable ->
            listOf("Failed to scrape ${target.url}: ${throwable.localizedMessage}")
        }
    }

    private fun extractHighlights(document: Document, selectors: List<String>): List<String> {
        val results = selectors.flatMap { selector ->
            document.select(selector)
                .mapNotNull { element ->
                    val text = element.text().trim()
                    if (text.length in 25..220) text else null
                }
        }
        return results.distinct().take(20)
    }
}

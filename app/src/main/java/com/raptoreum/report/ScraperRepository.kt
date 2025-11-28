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
            name = "Wallets",
            url = "https://raptoreum.com/latest",
            description = "Latest wallet and download news",
            cssSelectors = listOf("h1", "h2", "h3", "article", "section", "p")
        ),
        ScrapeTarget(
            name = "Official Site",
            url = "https://raptoreum.com/",
            description = "Ecosystem home with product highlights",
            cssSelectors = listOf("h1", "h2", "h3", "section", "article", "p")
        ),
        ScrapeTarget(
            name = "Documentation",
            url = "https://learn.raptoreum.com/",
            description = "End-user and builder documentation",
            cssSelectors = listOf("h1", "h2", "h3", "article", "section", "li")
        ),
        ScrapeTarget(
            name = "How to get RTM",
            url = "https://learn.raptoreum.com/how-to-get-raptoreum/",
            description = "Acquisition on-ramps and exchange listings",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "article")
        ),
        ScrapeTarget(
            name = "Whitepapers",
            url = "https://docs.raptoreum.com/",
            description = "Protocol papers and network deep dives",
            cssSelectors = listOf("h1", "h2", "h3", "p", "a", "li")
        ),
        ScrapeTarget(
            name = "GitHub",
            url = "https://github.com/Raptor3um/raptoreum",
            description = "Latest commits and release notes",
            cssSelectors = listOf("h1", "h2", "h3", "strong", "a", "span")
        ),
        ScrapeTarget(
            name = "RTM <> wRTM BSC Bridge",
            url = "https://bscbridge.raptoreum.com/",
            description = "Bridge status and swap details",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "wRTM BSC Contract",
            url = "https://bscscan.com/token/0xF7C71cab11E3694638Bb9A106E0F430565BD15F1",
            description = "Token contract analytics and holders",
            cssSelectors = listOf("h1", "h2", "h3", "table", "span", "div")
        ),
        ScrapeTarget(
            name = "X",
            url = "https://twitter.com/raptoreum",
            description = "Latest social posts and engagement",
            cssSelectors = listOf("article", "div[role=article]", "span")
        ),
        ScrapeTarget(
            name = "Instagram",
            url = "https://www.instagram.com/raptoreum/",
            description = "Instagram posts and captions",
            cssSelectors = listOf("h1", "h2", "h3", "article", "span")
        ),
        ScrapeTarget(
            name = "Facebook",
            url = "https://www.facebook.com/raptoreum/",
            description = "Facebook updates and community posts",
            cssSelectors = listOf("h1", "h2", "h3", "div[role=article]", "span")
        ),
        ScrapeTarget(
            name = "Bitcointalk ANN",
            url = "https://bitcointalk.org/index.php?topic=5065848",
            description = "Announcement thread and replies",
            cssSelectors = listOf("h1", "h2", "h3", "div", "p", "li")
        ),
        ScrapeTarget(
            name = "Telegram",
            url = "https://t.me/raptoreumm",
            description = "Community chat highlights",
            cssSelectors = listOf("h1", "h2", "h3", "p", "div")
        ),
        ScrapeTarget(
            name = "Reddit",
            url = "https://www.reddit.com/r/raptoreum/",
            description = "Community sentiment and daily chatter",
            cssSelectors = listOf("h1", "h2", "h3", "a[href*=comments]", "p")
        ),
        ScrapeTarget(
            name = "LinkedIn",
            url = "https://www.linkedin.com/company/raptoreum/",
            description = "LinkedIn updates and hiring signals",
            cssSelectors = listOf("h1", "h2", "h3", "p", "span")
        ),
        ScrapeTarget(
            name = "Blog",
            url = "https://blog.raptoreum.com/",
            description = "Official blog posts",
            cssSelectors = listOf("h1", "h2", "h3", "article", "section", "p")
        ),
        ScrapeTarget(
            name = "YouTube",
            url = "https://www.youtube.com/@RaptoreumOfficial",
            description = "Newest videos and descriptions",
            cssSelectors = listOf("h1", "h2", "h3", "a#video-title", "yt-formatted-string")
        ),
        ScrapeTarget(
            name = "TikTok",
            url = "https://www.tiktok.com/@raptoreumofficial",
            description = "TikTok clips and captions",
            cssSelectors = listOf("h1", "h2", "h3", "p", "strong")
        ),
        ScrapeTarget(
            name = "Discord",
            url = "https://discord.gg/raptoreum",
            description = "Community Discord invite and updates",
            cssSelectors = listOf("h1", "h2", "h3", "p", "span")
        ),
        ScrapeTarget(
            name = "Explorer",
            url = "https://explorer.raptoreum.com/",
            description = "Primary blockchain explorer",
            cssSelectors = listOf("h1", "h2", "h3", "div", "span")
        ),
        ScrapeTarget(
            name = "Explorer Backup",
            url = "https://chainz.cryptoid.info/rtm/",
            description = "Backup explorer stats",
            cssSelectors = listOf("h1", "h2", "h3", "table", "span", "p")
        ),
        ScrapeTarget(
            name = "gr_hash",
            url = "https://github.com/npq7721/gr_hash",
            description = "Mining algorithm repo",
            cssSelectors = listOf("h1", "h2", "h3", "strong", "p", "li")
        ),
        ScrapeTarget(
            name = "Electrumx",
            url = "https://github.com/Raptor3um/electrumx-RTM",
            description = "ElectrumX reference implementation",
            cssSelectors = listOf("h1", "h2", "h3", "strong", "p", "li")
        ),
        ScrapeTarget(
            name = "How to Mine",
            url = "https://docs.raptoreum.com/#/howtomine",
            description = "Mining how-to and setup guides",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "strong")
        ),
        ScrapeTarget(
            name = "Miners",
            url = "https://raptoreum.com/",
            description = "Mining downloads via site menu",
            cssSelectors = listOf("h1", "h2", "h3", "section", "article", "p")
        ),
        ScrapeTarget(
            name = "Hash & Gains Calculator",
            url = "https://mineraptoreum.com/",
            description = "Hash rate profitability and benchmarks",
            cssSelectors = listOf("h1", "h2", "h3", "p", "table", "div")
        ),
        ScrapeTarget(
            name = "Minerstat",
            url = "https://minerstat.com/coin/RTM",
            description = "Hash rate and profitability stats",
            cssSelectors = listOf("h1", "h2", "h3", "p", "table", "span")
        ),
        ScrapeTarget(
            name = "Raptorwings",
            url = "https://github.com/Raptor3um/RaptorWings/releases",
            description = "RaptorWings releases and notes",
            cssSelectors = listOf("h1", "h2", "h3", "a", "strong", "li")
        ),
        ScrapeTarget(
            name = "Shared Nodes",
            url = "https://inodez.com/",
            description = "Shared node provider status",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "Sullynode",
            url = "https://www.sullynode.com/",
            description = "Shared node host",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "Paper Wallet",
            url = "https://angainordev.github.io/VelociRaptor/",
            description = "Paper wallet generator",
            cssSelectors = listOf("h1", "h2", "h3", "p", "button", "strong")
        ),
        ScrapeTarget(
            name = "Raptoreum Portfolio",
            url = "https://raptoreum.io/",
            description = "Community portfolio site",
            cssSelectors = listOf("h1", "h2", "h3", "p", "section", "article")
        ),
        ScrapeTarget(
            name = "SaltyDragon",
            url = "https://saltydragon.io/",
            description = "Community game integration",
            cssSelectors = listOf("h1", "h2", "h3", "p", "section", "article")
        ),
        ScrapeTarget(
            name = "Ghostrider Valley",
            url = "https://ghostridervalleydemo.netlify.app/",
            description = "Ghostrider Valley community project",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "RTM Market",
            url = "https://rtmmarket.com/",
            description = "Marketplace updates",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "Shopify Store",
            url = "https://raptoreum.myshopify.com/",
            description = "Merch and product listings",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "span")
        ),
        ScrapeTarget(
            name = "CryptocurrencyCheckout",
            url = "https://cryptocurrencycheckout.com/coin/raptoreum",
            description = "Checkout integration details",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "Vidulum App",
            url = "https://vidulum.app/",
            description = "Vidulum wallet support",
            cssSelectors = listOf("h1", "h2", "h3", "p", "span", "section")
        ),
        ScrapeTarget(
            name = "Zelcore",
            url = "https://zelcore.io/",
            description = "Zelcore wallet integration",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "section")
        ),
        ScrapeTarget(
            name = "Gemlink",
            url = "https://gemlink.org/",
            description = "Gemlink partner wallet",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "Komodo Wallet",
            url = "https://komodoplatform.com/en/wallets.html",
            description = "Komodo wallet support",
            cssSelectors = listOf("h1", "h2", "h3", "p", "li", "div")
        ),
        ScrapeTarget(
            name = "Medium Blog",
            url = "https://medium.com/@raptoreum",
            description = "Community blog posts and development diaries",
            cssSelectors = listOf("h1", "h2", "h3", "article")
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

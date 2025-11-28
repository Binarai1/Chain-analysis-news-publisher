# Raptoreum ecosystem daily report

This repository now includes an Android application that scrapes public Raptoreum signals across the open web—no API keys required—and renders a shareable daily report. The original Python CLI remains available for scripted reporting and is documented below.

## Android application (Raptoreum Scout)

**Highlights**

- Open-web scraping of Raptoreum sources: official site, Reddit, X/Twitter, YouTube, CoinGecko, and CoinMarketCap using Jsoup.
- Live status panel shows scraping progress in real time as pages are fetched and parsed.
- One-tap “View Report” preview plus PDF export to device storage.
- Shareable via Facebook, X, LinkedIn, Reddit, or a generic target using per-platform icons.
- Compose UI with Material 3 styling for quick navigation.

**Running the app**

1. Open the project in Android Studio Giraffe or newer.
2. Use the included Gradle scripts to build and run the `app` module on a device/emulator (Android 8.0+/API 26).
3. Tap **Start Scan** to launch the web-wide scrape; watch live status messages, then view or save the generated report.
4. Tap a share icon to post the report text (or the exported PDF, once saved) to Facebook/X/LinkedIn/Reddit.

## What the tool collects

- **Blockchain status**: height, difficulty, peers and network hash rate from a configurable explorer API.
- **CoinGecko snapshot**: price, market cap, 24h change, community and developer metrics.
- **CoinMarketCap snapshot** (optional): price, market cap, volume and ranking when a Pro API key is configured.
- **Social media**:
  - X / Twitter follower and tweet counts for configured usernames (requires a bearer token).
  - Reddit subscriber and active counts for configured subreddits.
- **YouTube**: latest channel uploads with descriptions and transcripts when available.

## Configuration

Set environment variables to customize sources:

- `RAPTOREUM_REPORTS_DIR`: path where reports are written (default `reports`).
- `COINGECKO_ID`: CoinGecko identifier (default `raptoreum`).
- `COINMARKETCAP_SYMBOL`: CoinMarketCap ticker symbol (default `RTM`).
- `COINMARKETCAP_API_KEY`: Pro API key for CoinMarketCap (optional).
- `YOUTUBE_CHANNEL_ID`: channel ID for fetching the latest videos (required for YouTube section).
- `YOUTUBE_API_KEY`: YouTube Data API key (required for YouTube section).
- `TWITTER_BEARER_TOKEN`: bearer token for the X API (optional; without it the Twitter section is skipped).
- `TWITTER_USERNAMES`: comma-separated usernames to track (default `raptoreumproject`).
- `REDDIT_SUBREDDITS`: comma-separated subreddit names (default `Raptoreum`).
- `BLOCKCHAIN_API_BASE`: base URL for a block explorer compatible with `/status?q=getInfo` (default `https://blockexplorer.raptoreum.com/api`).

## Usage

Install dependencies and run the CLI:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python -m raptoreum_report.main --max-videos 5
```

Each execution writes a Markdown file named `raptoreum-report-YYYY-MM-DD.md` inside `reports/` (or the configured directory). You can schedule the script with cron or a CI workflow to generate the report daily.

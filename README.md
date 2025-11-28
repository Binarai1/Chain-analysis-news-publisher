# Raptoreum ecosystem daily report

This repository now includes an Android application that scrapes public Raptoreum signals across the open web—no API keys required—and renders a shareable daily report. The original Python CLI remains available for scripted reporting and is documented below.

## Android application (Raptoreum Scout)

**Highlights**

- Open-web scraping across all official and community touchpoints: wallets, docs, explorers, bridges, stores, social feeds (X, Reddit, Facebook, Instagram, LinkedIn), ANN threads, wRTM bridge + contract, GitHub, Medium, CoinGecko, CoinMarketCap, community projects, and more—all with Jsoup and no API keys required.
- AI-generated insight layer (optional): point the app at a free OpenAI-compatible endpoint to turn raw highlights into a crisp briefing. A local fallback stays active when no endpoint is configured.
- Reports include pulled Raptoreum logos/graphics and a multi-source narrative. Live status panel shows scraping progress in real time as pages are fetched and parsed.
- One-tap “View Report” preview plus PDF export to device storage.
- Shareable via Facebook, X, LinkedIn, Reddit, or a generic target using per-platform icons.
- Compose UI with Material 3 styling for quick navigation.

## Ideas for game-changing upgrades

- **Hybrid crawler mesh**: pair the on-device scraper with an optional cloud/edge worker that runs the same Jsoup pipelines in parallel, de-duplicates with Bloom filters, and streams interim findings back to the app’s live status window for near–real time coverage of the wider web.
- **On-device signal scoring**: ship lightweight TensorFlow Lite models to rank posts/videos by reach, engagement velocity, and sentiment, then surface only the top-ranked items in the daily report to keep it concise while still comprehensive.
- **Evidence preservation**: capture HTML snapshots and lightweight screenshots of scraped pages, bundle them with hashes, and let users tap into a “proof” view in the report so any claim can be verified or rechecked later—even if the source page is edited or deleted.
- **Change detection & alerts**: track deltas (e.g., follower surges, difficulty jumps, new exchange listings) and trigger user-configurable push notifications when thresholds are crossed; background this work with WorkManager so alerts arrive without opening the app.
- **Interactive insights**: render small inline charts (price vs. social mentions, hash rate vs. sentiment) using Compose graphics, and let users drill into a timeline view for each signal with filters for exchange, network, and social source.
- **Offline-first library**: cache all scraped artifacts locally with a compact full-text index so reports stay browsable and searchable offline; sync diffs when connectivity returns to reconcile with the cloud worker’s archive.

**Running the app**

1. Open the project in Android Studio Giraffe or newer.
2. Use the included Gradle scripts to build and run the `app` module on a device/emulator (Android 8.0+/API 26).
3. Tap **Start Scan** to launch the web-wide scrape; watch live status messages, then view or save the generated report.
4. Tap a share icon to post the report text (or the exported PDF, once saved) to Facebook/X/LinkedIn/Reddit.

### Building and testing from Termux

Use the bundled helper script when building directly on an Android device with Termux. It provisions OpenJDK, downloads the Android SDK command-line tools, and runs Gradle tasks for you:

```bash
bash scripts/termux-setup.sh          # installs toolchain, builds the debug APK, and runs unit tests
bash scripts/termux-setup.sh --no-build   # installs toolchain only
bash scripts/termux-setup.sh --skip-tests # builds APK without running Gradle tests
```

After completion, the debug APK is located at `app/build/outputs/apk/debug/`.

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

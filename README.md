# Raptoreum ecosystem daily report

This repository now includes an Android application that scrapes public Raptoreum signals across the open web—no API keys required—and renders a shareable daily report. The original Python CLI remains available for scripted reporting and is documented below.

## Android application (Raptoreum Scout)

**Highlights**

- Open-web scraping of Raptoreum sources: official site, Reddit, X/Twitter, YouTube, CoinGecko, and CoinMarketCap using Jsoup.
- Live status panel shows scraping progress in real time as pages are fetched and parsed.
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

> **Note:** The repository no longer commits the binary `gradle/wrapper/gradle-wrapper.jar`. When you invoke `./gradlew`, the script now downloads the official wrapper JAR from the Gradle GitHub mirror and verifies its SHA-256 hash before proceeding. This keeps pull requests text-only while still preserving a reproducible wrapper experience.

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

## Termux one-shot setup & APK build

On Android with [Termux](https://termux.dev/), run the helper script to provision the full toolchain (OpenJDK, Android SDK command-line tools, and Gradle) and then build the APK:

```bash
# from the repo root
bash scripts/termux-setup.sh
```

What the script does:

- Updates Termux packages and installs OpenJDK 17, Gradle, Python, unzip/zip, curl/wget, and supporting tools.
- Downloads the Android SDK command-line tools, installs platform-tools, platform **android-34**, and **build-tools 34.0.0**, and auto-accepts licenses.
- Persists `ANDROID_SDK_ROOT`, `JAVA_HOME`, and PATH entries into `~/.profile` for future sessions.
- Runs `./gradlew --no-daemon assembleDebug` to emit `app/build/outputs/apk/debug/app-debug.apk`.

You can skip the build step (for offline prep) with `bash scripts/termux-setup.sh --no-build`.

### Gradle download fallback

If the Gradle wrapper cannot fetch its distribution because of network egress limits, you can pre-seed the cache using `curl` a
nd rerun the build offline:

1. Download the distribution zip referenced in [`gradle/wrapper/gradle-wrapper.properties`](gradle/wrapper/gradle-wrapper.prope
rties):

   ```bash
   curl -L -o /tmp/gradle-8.14.3-bin.zip https://services.gradle.org/distributions/gradle-8.14.3-bin.zip
   ```

2. Create the Gradle wrapper cache directory if it does not exist and place the zip there (replace the hash directory if your e
nvironment generates a different one):

   ```bash
   mkdir -p ~/.gradle/wrapper/dists/gradle-8.14.3-bin/cv11ve7ro1n3o1j4so8xd9n66/
   cp /tmp/gradle-8.14.3-bin.zip ~/.gradle/wrapper/dists/gradle-8.14.3-bin/cv11ve7ro1n3o1j4so8xd9n66/
   ```

3. Retry the build with the cached distribution (add `--offline` if repository access is blocked):

   ```bash
   GRADLE_OPTS='-Djava.net.preferIPv4Stack=true' ./gradlew --no-daemon assembleDebug
   ```

This avoids the wrapper’s initial download and lets you proceed when only limited HTTP clients are allowed.

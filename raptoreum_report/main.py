from __future__ import annotations

import argparse
import sys

from .collectors.blockchain import fetch_blockchain_status
from .collectors.coingecko import fetch_coingecko_snapshot
from .collectors.coinmarketcap import fetch_coinmarketcap_snapshot, CoinMarketCapError
from .collectors.social_media import fetch_twitter_profiles, fetch_reddit_stats
from .collectors.youtube import fetch_latest_videos, YouTubeCollectorError
from .config import DEFAULT_CONFIG, ReportConfig
from .report_builder import build_report, save_report


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate the daily Raptoreum ecosystem report.")
    parser.add_argument("--coingecko-id", default=DEFAULT_CONFIG.coingecko_id)
    parser.add_argument("--cmc-symbol", default=DEFAULT_CONFIG.coinmarketcap_symbol)
    parser.add_argument("--reports-dir", default=str(DEFAULT_CONFIG.reports_dir))
    parser.add_argument("--max-videos", type=int, default=3)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    config = ReportConfig(
        reports_dir=args.reports_dir,
        coingecko_id=args.coingecko_id,
        coinmarketcap_symbol=args.cmc_symbol,
    )

    blockchain = fetch_blockchain_status(config.blockchain_api_base)
    coingecko = fetch_coingecko_snapshot(config.coingecko_id)

    cmc_data = None
    if config.coinmarketcap_api_key:
        try:
            cmc_data = fetch_coinmarketcap_snapshot(
                config.coinmarketcap_symbol, config.coinmarketcap_api_key
            )
        except CoinMarketCapError as exc:
            print(f"CoinMarketCap error: {exc}", file=sys.stderr)

    twitter_profiles = fetch_twitter_profiles(config.twitter_usernames, config.twitter_bearer_token)
    reddit_stats = fetch_reddit_stats(config.reddit_subreddits)

    videos = []
    if config.youtube_channel_id and config.youtube_api_key:
        try:
            videos = fetch_latest_videos(
                config.youtube_channel_id, config.youtube_api_key, max_results=args.max_videos
            )
        except YouTubeCollectorError as exc:
            print(f"YouTube error: {exc}", file=sys.stderr)

    report = build_report(config, blockchain, coingecko, cmc_data, twitter_profiles, reddit_stats, videos)
    output_file = save_report(report, config)
    print(f"Report saved to {output_file}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

from __future__ import annotations

from datetime import datetime
from pathlib import Path
from typing import Dict, Any, List

from .config import ReportConfig


def render_section(title: str, lines: List[str]) -> str:
    body = "\n".join(lines)
    return f"## {title}\n\n{body}\n"


def build_report(
    config: ReportConfig,
    blockchain: Dict[str, Any],
    coingecko: Dict[str, Any],
    coinmarketcap: Dict[str, Any] | None,
    twitter_profiles: List[Dict[str, Any]],
    reddit_stats: List[Dict[str, Any]],
    videos: List[Dict[str, Any]],
) -> str:
    """Create a markdown report from collected sources."""
    sections: List[str] = []

    sections.append(
        render_section(
            "Blockchain", [
                f"- Height: {blockchain.get('blocks')}",
                f"- Difficulty: {blockchain.get('difficulty')}",
                f"- Connections: {blockchain.get('connections')}",
                f"- Network hash rate: {blockchain.get('networkhashps')}",
            ],
        )
    )

    sections.append(
        render_section(
            "CoinGecko",
            [
                f"- Price (USD): {coingecko.get('price_usd')}",
                f"- Market Cap (USD): {coingecko.get('market_cap_usd')}",
                f"- 24h Change: {coingecko.get('price_change_24h_pct')}%",
                f"- Developer Stars: {coingecko.get('developer_data', {}).get('stars')}",
                f"- Community Score: {coingecko.get('community_data', {}).get('community_score')}",
            ],
        )
    )

    if coinmarketcap:
        sections.append(
            render_section(
                "CoinMarketCap",
                [
                    f"- Price (USD): {coinmarketcap.get('price_usd')}",
                    f"- Market Cap (USD): {coinmarketcap.get('market_cap_usd')}",
                    f"- 24h Change: {coinmarketcap.get('percent_change_24h')}%",
                    f"- Rank: {coinmarketcap.get('cmc_rank')}",
                    f"- Links: {coinmarketcap.get('urls')}",
                ],
            )
        )

    if twitter_profiles:
        sections.append(
            render_section(
                "X / Twitter",
                [
                    f"- @{profile['username']}: {profile['followers']} followers, {profile['tweet_count']} tweets"
                    for profile in twitter_profiles
                ],
            )
        )

    if reddit_stats:
        sections.append(
            render_section(
                "Reddit",
                [
                    f"- r/{sub['name']}: {sub['subscribers']} subscribers, {sub['active_user_count']} active"
                    for sub in reddit_stats
                ],
            )
        )

    if videos:
        lines = []
        for video in videos:
            published = video.get("published_at")
            published_str = published.strftime("%Y-%m-%d") if published else "Unknown"
            lines.append(
                f"- [{video.get('title')}]({video.get('url')}) — {published_str}\n  {video.get('description')}\n  Transcript: {('present' if video.get('transcript') else 'missing')}"
            )
        sections.append(render_section("YouTube", lines))

    created_on = datetime.utcnow().strftime("%Y-%m-%d")
    heading = f"# Raptoreum Daily Report ({created_on})\n\n"
    return heading + "\n".join(sections)


def save_report(report: str, config: ReportConfig) -> Path:
    config.reports_dir.mkdir(parents=True, exist_ok=True)
    filename = config.reports_dir / f"raptoreum-report-{datetime.utcnow().date()}.md"
    filename.write_text(report)
    return filename

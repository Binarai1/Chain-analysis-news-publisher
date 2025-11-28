from __future__ import annotations

from dataclasses import dataclass, field
import os
from pathlib import Path
from typing import List


@dataclass
class ReportConfig:
    """Configuration for the Raptoreum daily report."""

    reports_dir: Path = Path(os.environ.get("RAPTOREUM_REPORTS_DIR", "reports"))
    coingecko_id: str = os.environ.get("COINGECKO_ID", "raptoreum")
    coinmarketcap_symbol: str = os.environ.get("COINMARKETCAP_SYMBOL", "RTM")
    coinmarketcap_api_key: str | None = os.environ.get("COINMARKETCAP_API_KEY")
    youtube_channel_id: str | None = os.environ.get("YOUTUBE_CHANNEL_ID")
    youtube_api_key: str | None = os.environ.get("YOUTUBE_API_KEY")
    twitter_bearer_token: str | None = os.environ.get("TWITTER_BEARER_TOKEN")
    twitter_usernames: List[str] = field(
        default_factory=lambda: os.environ.get("TWITTER_USERNAMES", "raptoreumproject").split(",")
    )
    reddit_subreddits: List[str] = field(
        default_factory=lambda: os.environ.get("REDDIT_SUBREDDITS", "Raptoreum").split(",")
    )
    blockchain_api_base: str = os.environ.get(
        "BLOCKCHAIN_API_BASE", "https://blockexplorer.raptoreum.com/api"
    )


DEFAULT_CONFIG = ReportConfig()

from __future__ import annotations

import requests
from typing import Dict, Any


class CoinMarketCapError(RuntimeError):
    pass


def fetch_coinmarketcap_snapshot(symbol: str, api_key: str) -> Dict[str, Any]:
    """Fetch price and social stats from CoinMarketCap."""
    url = "https://pro-api.coinmarketcap.com/v2/cryptocurrency/quotes/latest"
    params = {"symbol": symbol}
    headers = {"X-CMC_PRO_API_KEY": api_key}
    response = requests.get(url, headers=headers, params=params, timeout=30)
    if response.status_code == 401:
        raise CoinMarketCapError("CoinMarketCap API key is invalid or missing permissions")
    response.raise_for_status()
    payload = response.json()
    data = payload.get("data", {}).get(symbol, [])
    listing = data[0] if isinstance(data, list) and data else {}
    quote = listing.get("quote", {}).get("USD", {})

    return {
        "name": listing.get("name"),
        "symbol": listing.get("symbol"),
        "price_usd": quote.get("price"),
        "market_cap_usd": quote.get("market_cap"),
        "volume_24h_usd": quote.get("volume_24h"),
        "percent_change_24h": quote.get("percent_change_24h"),
        "cmc_rank": listing.get("cmc_rank"),
        "twitter": listing.get("twitter_username"),
        "urls": listing.get("urls", {}),
    }

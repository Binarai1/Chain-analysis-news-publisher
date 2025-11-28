from __future__ import annotations

import requests
from typing import Dict, Any


def fetch_coingecko_snapshot(coin_id: str) -> Dict[str, Any]:
    """Retrieve market data for a coin from CoinGecko."""
    url = f"https://api.coingecko.com/api/v3/coins/{coin_id}"
    response = requests.get(url, timeout=30)
    response.raise_for_status()
    payload = response.json()
    market_data = payload.get("market_data", {})
    community_data = payload.get("community_data", {})
    developer_data = payload.get("developer_data", {})

    return {
        "name": payload.get("name"),
        "symbol": payload.get("symbol"),
        "price_usd": market_data.get("current_price", {}).get("usd"),
        "market_cap_usd": market_data.get("market_cap", {}).get("usd"),
        "price_change_24h_pct": market_data.get("price_change_percentage_24h"),
        "links": payload.get("links", {}),
        "community_data": community_data,
        "developer_data": developer_data,
    }

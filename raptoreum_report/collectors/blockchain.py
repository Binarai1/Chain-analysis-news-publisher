from __future__ import annotations

from typing import Dict, Any

import requests


def fetch_blockchain_status(api_base: str) -> Dict[str, Any]:
    """Collect basic blockchain metrics from a Raptoreum explorer API."""
    status_url = f"{api_base}/status?q=getInfo"
    response = requests.get(status_url, timeout=30)
    response.raise_for_status()
    payload = response.json()
    info = payload.get("info", {})

    return {
        "blocks": info.get("blocks"),
        "difficulty": info.get("difficulty"),
        "connections": info.get("connections"),
        "networkhashps": info.get("networkhashps"),
        "proxy": info.get("proxy"),
    }

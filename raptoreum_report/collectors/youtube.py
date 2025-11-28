from __future__ import annotations

from datetime import datetime
from typing import Dict, Any, List

import requests
from youtube_transcript_api import YouTubeTranscriptApi


class YouTubeCollectorError(RuntimeError):
    pass


def fetch_latest_videos(channel_id: str, api_key: str, max_results: int = 3) -> List[Dict[str, Any]]:
    """Return latest videos with transcripts when available."""
    search_url = "https://www.googleapis.com/youtube/v3/search"
    search_params = {
        "part": "snippet",
        "channelId": channel_id,
        "maxResults": max_results,
        "order": "date",
        "type": "video",
        "key": api_key,
    }
    response = requests.get(search_url, params=search_params, timeout=30)
    if response.status_code == 403:
        raise YouTubeCollectorError("YouTube API key appears to be invalid or rate limited")
    response.raise_for_status()
    payload = response.json()
    videos = []
    for item in payload.get("items", []):
        video_id = item["id"]["videoId"]
        snippet = item.get("snippet", {})
        published_at = snippet.get("publishedAt")
        published = (
            datetime.fromisoformat(published_at.replace("Z", "+00:00"))
            if published_at
            else None
        )
        transcript = None
        try:
            parts = YouTubeTranscriptApi.get_transcript(video_id)
            transcript = " ".join([chunk.get("text", "") for chunk in parts])
        except Exception:
            transcript = None

        videos.append(
            {
                "id": video_id,
                "title": snippet.get("title"),
                "description": snippet.get("description"),
                "published_at": published,
                "transcript": transcript,
                "url": f"https://www.youtube.com/watch?v={video_id}",
            }
        )

    return videos

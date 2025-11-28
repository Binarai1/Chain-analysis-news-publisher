from __future__ import annotations

from typing import Dict, Any, List

import requests


def fetch_twitter_profiles(usernames: List[str], bearer_token: str | None) -> List[Dict[str, Any]]:
    """Fetch follower and engagement metrics for a list of X (Twitter) usernames."""
    if not bearer_token:
        return []

    profiles = []
    for username in filter(None, usernames):
        url = f"https://api.twitter.com/2/users/by/username/{username}"
        params = {"user.fields": "public_metrics"}
        headers = {"Authorization": f"Bearer {bearer_token}"}
        response = requests.get(url, params=params, headers=headers, timeout=30)
        response.raise_for_status()
        payload = response.json().get("data", {})
        metrics = payload.get("public_metrics", {})
        profiles.append(
            {
                "username": username,
                "followers": metrics.get("followers_count"),
                "following": metrics.get("following_count"),
                "tweet_count": metrics.get("tweet_count"),
                "listed_count": metrics.get("listed_count"),
                "profile_url": f"https://twitter.com/{username}",
            }
        )
    return profiles


def fetch_reddit_stats(subreddits: List[str]) -> List[Dict[str, Any]]:
    """Fetch subscriber counts for configured subreddits."""
    stats = []
    for subreddit in filter(None, subreddits):
        url = f"https://www.reddit.com/r/{subreddit}/about.json"
        headers = {"User-Agent": "raptoreum-report-bot/0.1"}
        response = requests.get(url, headers=headers, timeout=30)
        if response.status_code == 429:
            continue
        response.raise_for_status()
        data = response.json().get("data", {})
        stats.append(
            {
                "name": subreddit,
                "subscribers": data.get("subscribers"),
                "active_user_count": data.get("accounts_active"),
                "description": data.get("public_description"),
                "url": f"https://www.reddit.com/r/{subreddit}",
            }
        )
    return stats

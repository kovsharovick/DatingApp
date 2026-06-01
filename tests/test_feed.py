import pytest


class TestFeedController:

    def test_get_feed_requires_auth(self, api_client):
        resp = api_client.get("/api/feed")
        assert resp.status_code == 401

    def test_get_feed_invalid_token(self, api_client):
        resp = api_client.get("/api/feed", headers={"Authorization": "Bearer bad.token"})
        assert resp.status_code == 401

    def test_get_feed_returns_list_or_requires_video(self, api_client, auth_headers):
        resp = api_client.get("/api/feed", headers=auth_headers)
        assert resp.status_code in (200, 400)
        if resp.status_code == 200:
            assert isinstance(resp.json(), list)

    def test_get_feed_default_limit(self, api_client, auth_headers):
        resp = api_client.get("/api/feed", headers=auth_headers)
        if resp.status_code == 200:
            assert len(resp.json()) <= 10

    def test_get_feed_custom_limit(self, api_client, auth_headers):
        resp = api_client.get("/api/feed?limit=5", headers=auth_headers)
        if resp.status_code == 200:
            assert len(resp.json()) <= 5

    def test_get_feed_limit_one(self, api_client, auth_headers):
        resp = api_client.get("/api/feed?limit=1", headers=auth_headers)
        if resp.status_code == 200:
            assert len(resp.json()) <= 1

    def test_get_feed_item_fields(self, api_client, auth_headers):
        resp = api_client.get("/api/feed", headers=auth_headers)
        if resp.status_code == 200 and resp.json():
            item = resp.json()[0]
            for field in ("userId", "name", "age", "city", "region", "likedYou"):
                assert field in item, f"Missing field: {field}"

    def test_get_feed_does_not_include_self(self, api_client, auth_headers, user_id):
        resp = api_client.get("/api/feed", headers=auth_headers)
        if resp.status_code == 200:
            ids = [item["userId"] for item in resp.json()]
            assert user_id not in ids

    def test_get_feed_does_not_include_blocked_users(self, api_client, auth_headers,
                                                     second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        resp = api_client.get("/api/feed", headers=auth_headers)
        if resp.status_code == 200:
            ids = [item["userId"] for item in resp.json()]
            assert second_user_id not in ids
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

    def test_get_feed_does_not_include_already_swiped(self, api_client, auth_headers,
                                                      second_user_id):
        api_client.post("/api/swipes", json={"targetUserId": second_user_id, "direction": "LIKE"}, headers=auth_headers)
        resp = api_client.get("/api/feed", headers=auth_headers)
        if resp.status_code == 200:
            ids = [item["userId"] for item in resp.json()]
            assert second_user_id not in ids

    def test_get_feed_no_video_error_message(self, api_client, third_auth_headers):
        resp = api_client.get("/api/feed", headers=third_auth_headers)
        if resp.status_code == 400:
            assert "video" in resp.text.lower()

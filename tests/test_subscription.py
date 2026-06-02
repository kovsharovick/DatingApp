import pytest


class TestSubscriptionController:
    def test_get_subscription_success(self, api_client, auth_headers):
        resp = api_client.get("/api/subscription", headers=auth_headers)
        assert resp.status_code == 200

    def test_get_subscription_unauthorized(self, api_client):
        resp = api_client.get("/api/subscription")
        assert resp.status_code == 401

    def test_get_subscription_fields(self, api_client, auth_headers):
        resp = api_client.get("/api/subscription", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        for field in ("plan", "effectivePlan", "startedAt", "expiresAt", "likesRemaining"):
            assert field in data, f"Missing field: {field}"

    def test_new_user_has_free_plan(self, api_client, auth_headers):
        resp = api_client.get("/api/subscription", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        assert data["plan"] in ("FREE", "PREMIUM")

    def test_likes_remaining_is_non_negative(self, api_client, auth_headers):
        resp = api_client.get("/api/subscription", headers=auth_headers)
        assert resp.status_code == 200
        assert resp.json()["likesRemaining"] >= 0

    def test_free_plan_likes_remaining_at_most_5(self, api_client, third_auth_headers):
        resp = api_client.get("/api/subscription", headers=third_auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        if data["effectivePlan"] == "FREE":
            assert data["likesRemaining"] <= 5

    def test_activate_premium_success(self, api_client, auth_headers):
        resp = api_client.post("/api/subscription/premium", headers=auth_headers)
        assert resp.status_code == 200
        assert "message" in resp.json()
        assert "premium" in resp.json()["message"].lower()

    def test_activate_premium_unauthorized(self, api_client):
        resp = api_client.post("/api/subscription/premium")
        assert resp.status_code == 401

    def test_activate_premium_default_30_days(self, api_client, auth_headers):
        resp = api_client.post("/api/subscription/premium", headers=auth_headers)
        assert resp.status_code == 200
        assert "30" in resp.json()["message"]

    def test_activate_premium_custom_days(self, api_client, auth_headers):
        resp = api_client.post("/api/subscription/premium?days=7", headers=auth_headers)
        assert resp.status_code == 200
        assert "7" in resp.json()["message"]

    def test_after_premium_plan_is_premium(self, api_client, auth_headers):
        api_client.post("/api/subscription/premium", headers=auth_headers)
        resp = api_client.get("/api/subscription", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        assert data["effectivePlan"] == "PREMIUM"

    def test_premium_increases_likes_remaining(self, api_client, second_auth_headers):
        before = api_client.get("/api/subscription", headers=second_auth_headers).json()["likesRemaining"]
        api_client.post("/api/subscription/premium", headers=second_auth_headers)
        after = api_client.get("/api/subscription", headers=second_auth_headers).json()["likesRemaining"]
        assert after >= before

    def test_activate_premium_expires_at_set(self, api_client, auth_headers):
        api_client.post("/api/subscription/premium", headers=auth_headers)
        data = api_client.get("/api/subscription", headers=auth_headers).json()
        assert data["expiresAt"] != "never"

    def test_activate_premium_extends_existing_expiry(self, api_client, third_auth_headers):
        api_client.post("/api/subscription/premium?days=30", headers=third_auth_headers)
        first = api_client.get("/api/subscription", headers=third_auth_headers).json()["expiresAt"]
        api_client.post("/api/subscription/premium?days=30", headers=third_auth_headers)
        second = api_client.get("/api/subscription", headers=third_auth_headers).json()["expiresAt"]
        assert second > first

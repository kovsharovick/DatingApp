import pytest
import time


class TestBlockController:
    def test_get_blocked_empty(self, api_client, auth_headers):
        resp = api_client.get("/api/blocks", headers=auth_headers)
        assert resp.status_code == 200
        assert isinstance(resp.json(), list)

    def test_get_blocked_unauthorized(self, api_client):
        resp = api_client.get("/api/blocks")
        assert resp.status_code == 401

    def test_get_blocked_contains_blocked_user(self, api_client, auth_headers, second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        resp = api_client.get("/api/blocks", headers=auth_headers)
        assert resp.status_code == 200
        ids = [u["userId"] for u in resp.json()]
        assert second_user_id in ids
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

    def test_get_blocked_response_fields(self, api_client, auth_headers, second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        resp = api_client.get("/api/blocks", headers=auth_headers)
        assert resp.status_code == 200
        entry = next((u for u in resp.json() if u["userId"] == second_user_id), None)
        assert entry is not None
        assert "userId" in entry
        assert "name" in entry
        assert "blockedAt" in entry
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

    def test_block_user_success(self, api_client, auth_headers, second_user_id):
        resp = api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        assert resp.status_code == 204
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

    def test_block_user_unauthorized(self, api_client, second_user_id):
        resp = api_client.post("/api/blocks", json={"blockedUserId": second_user_id})
        assert resp.status_code == 401

    def test_block_self_returns_error(self, api_client, auth_headers, user_id):
        resp = api_client.post("/api/blocks", json={"blockedUserId": user_id}, headers=auth_headers)
        assert resp.status_code == 400
        assert "yourself" in resp.text.lower() or "self" in resp.text.lower()

    def test_block_same_user_twice_returns_conflict(self, api_client, auth_headers, second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        resp = api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        assert resp.status_code == 409
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

    def test_block_missing_body(self, api_client, auth_headers):
        resp = api_client.post("/api/blocks", json={}, headers=auth_headers)
        assert resp.status_code == 400

    def test_block_null_user_id(self, api_client, auth_headers):
        resp = api_client.post("/api/blocks", json={"blockedUserId": None}, headers=auth_headers)
        assert resp.status_code == 400

    def test_unblock_user_success(self, api_client, auth_headers, second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        resp = api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)
        assert resp.status_code == 204

    def test_unblock_user_no_longer_in_list(self, api_client, auth_headers, second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)
        resp = api_client.get("/api/blocks", headers=auth_headers)
        ids = [u["userId"] for u in resp.json()]
        assert second_user_id not in ids

    def test_unblock_non_blocked_user_is_idempotent(self, api_client, auth_headers, second_user_id):
        resp = api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)
        assert resp.status_code == 204

    def test_unblock_unauthorized(self, api_client, second_user_id):
        resp = api_client.delete(f"/api/blocks/{second_user_id}")
        assert resp.status_code == 401

    def test_blocked_user_cannot_see_blocker_profile(self, api_client, auth_headers, user_id,
                                                     second_user_id, second_auth_headers):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        resp = api_client.get(f"/api/users/{user_id}/profile", headers=second_auth_headers)
        assert resp.status_code == 400
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

    def test_re_block_after_unblock(self, api_client, auth_headers, second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)
        resp = api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        assert resp.status_code == 204
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

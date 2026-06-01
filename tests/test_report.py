import pytest


class TestReportController:

    def test_report_user_success(self, api_client, auth_headers, second_user_id):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": second_user_id,
            "reason": "Inappropriate content"
        }, headers=auth_headers)
        assert resp.status_code == 204

    def test_report_user_unauthorized(self, api_client, second_user_id):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": second_user_id,
            "reason": "Spam"
        })
        assert resp.status_code == 401

    def test_report_self_returns_error(self, api_client, auth_headers, user_id):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": user_id,
            "reason": "Testing self-report"
        }, headers=auth_headers)
        assert resp.status_code == 400
        assert "yourself" in resp.text.lower() or "self" in resp.text.lower()

    def test_report_same_user_twice_returns_conflict(self, api_client, auth_headers, third_user_id):
        api_client.post("/api/reports", json={
            "reportedUserId": third_user_id,
            "reason": "First report"
        }, headers=auth_headers)
        resp = api_client.post("/api/reports", json={
            "reportedUserId": third_user_id,
            "reason": "Second report"
        }, headers=auth_headers)
        assert resp.status_code == 409

    def test_report_missing_reason(self, api_client, auth_headers, second_user_id):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": second_user_id
        }, headers=auth_headers)
        assert resp.status_code == 400

    def test_report_blank_reason(self, api_client, auth_headers, second_user_id):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": second_user_id,
            "reason": "   "
        }, headers=auth_headers)
        assert resp.status_code == 400

    def test_report_reason_too_long(self, api_client, auth_headers, second_user_id):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": second_user_id,
            "reason": "x" * 256
        }, headers=auth_headers)
        assert resp.status_code == 400

    def test_report_reason_exactly_255_chars(self, api_client, auth_headers, second_user_id):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": second_user_id,
            "reason": "x" * 255
        }, headers=auth_headers)
        assert resp.status_code in (204, 409)

    def test_report_missing_reported_user_id(self, api_client, auth_headers):
        resp = api_client.post("/api/reports", json={
            "reason": "Some reason"
        }, headers=auth_headers)
        assert resp.status_code == 400

    def test_report_null_reported_user_id(self, api_client, auth_headers):
        resp = api_client.post("/api/reports", json={
            "reportedUserId": None,
            "reason": "Some reason"
        }, headers=auth_headers)
        assert resp.status_code == 400

    def test_report_empty_body(self, api_client, auth_headers):
        resp = api_client.post("/api/reports", json={}, headers=auth_headers)
        assert resp.status_code == 400

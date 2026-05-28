import pytest
from tests.utils import register_user, login_user


class TestAuthController:

    def test_register_success(self, api_client, unique_email):
        userId, token = register_user(api_client, unique_email)
        assert userId is not None
        assert token is not None

    def test_login_success(self, api_client, unique_email):
        email = unique_email
        register_user(api_client, email)
        userId, token = login_user(api_client, email, "secret123")
        assert userId is not None
        assert token is not None

    def test_register_duplicate_email(self, api_client, unique_email):
        email = unique_email
        register_user(api_client, email)
        resp = api_client.post("/api/auth/register", json={
            "email": email,
            "password": "valid123",
            "name": "Name",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400
        assert "already in use" in resp.text.lower()

    def test_register_invalid_email(self, api_client):
        resp = api_client.post("/api/auth/register", json={
            "email": "not-an-email",
            "password": "valid123",
            "name": "Name",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_register_short_password(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "12345",
            "name": "Name",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400
        assert "password" in resp.text.lower()

    def test_register_missing_required_fields(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_register_age_under_18(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Young",
            "dateOfBirth": "2010-01-01",
            "gender": "FEMALE",
            "city": "Москва"
        })
        assert resp.status_code == 400
        assert "18" in resp.text or "age" in resp.text.lower()

    def test_register_invalid_city(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "NonExistentCity"
        })
        assert resp.status_code == 400
        assert "city not found" in resp.text.lower()

    def test_login_wrong_password(self, api_client, unique_email):
        register_user(api_client, unique_email)
        resp = api_client.post("/api/auth/login", json={
            "email": unique_email,
            "password": "wrongpass"
        })
        assert resp.status_code == 400

    def test_login_nonexistent_email(self, api_client):
        resp = api_client.post("/api/auth/login", json={
            "email": "nonexistent@example.com",
            "password": "secret123"
        })
        assert resp.status_code == 400

    def test_login_missing_credentials(self, api_client):
        resp = api_client.post("/api/auth/login", json={})
        assert resp.status_code == 400

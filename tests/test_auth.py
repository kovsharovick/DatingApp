import pytest
from tests.utils import register_user, login_user


class TestAuthController:
    def test_register_success(self, api_client, unique_email):
        userId, token = register_user(api_client, unique_email)
        assert userId is not None
        assert token is not None

    def test_register_returns_bearer_token_type(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "1995-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 201
        assert resp.json().get("tokenType") == "Bearer"

    def test_register_returns_201(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "1995-01-01",
            "gender": "FEMALE",
            "city": "Москва"
        })
        assert resp.status_code == 201

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

    def test_register_missing_name(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_register_missing_email(self, api_client):
        resp = api_client.post("/api/auth/register", json={
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_register_missing_password(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "name": "Tester",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_register_missing_required_fields(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_register_empty_body(self, api_client):
        resp = api_client.post("/api/auth/register", json={})
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

    def test_register_exactly_18(self, api_client, unique_email):
        import datetime
        dob = (datetime.date.today().replace(year=datetime.date.today().year - 18)).isoformat()
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Adult",
            "dateOfBirth": dob,
            "gender": "MALE",
            "city": "Москва"
        })
        assert resp.status_code == 201

    def test_register_invalid_city(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "2000-01-01",
            "gender": "MALE",
            "city": "NonExistentCity_XYZ_123"
        })
        assert resp.status_code == 400
        assert "city not found" in resp.text.lower()

    def test_register_invalid_gender(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "2000-01-01",
            "gender": "UNKNOWN",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_register_with_optional_fields(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "1995-06-15",
            "gender": "FEMALE",
            "city": "Москва",
            "description": "Hello world",
            "minAge": 20,
            "maxAge": 35,
            "radiusKm": 100,
            "preferredGenders": ["MALE"]
        })
        assert resp.status_code == 201

    def test_register_invalid_date_format(self, api_client, unique_email):
        resp = api_client.post("/api/auth/register", json={
            "email": unique_email,
            "password": "secret123",
            "name": "Tester",
            "dateOfBirth": "15-06-1995",
            "gender": "FEMALE",
            "city": "Москва"
        })
        assert resp.status_code == 400

    def test_login_success(self, api_client, unique_email):
        email = unique_email
        register_user(api_client, email)
        userId, token = login_user(api_client, email, "secret123")
        assert userId is not None
        assert token is not None

    def test_login_returns_200(self, api_client, unique_email):
        register_user(api_client, unique_email)
        resp = api_client.post("/api/auth/login", json={
            "email": unique_email,
            "password": "secret123"
        })
        assert resp.status_code == 200

    def test_login_returns_user_id_and_token(self, api_client, unique_email):
        register_user(api_client, unique_email)
        resp = api_client.post("/api/auth/login", json={
            "email": unique_email,
            "password": "secret123"
        })
        data = resp.json()
        assert "userId" in data
        assert "token" in data

    def test_login_wrong_password(self, api_client, unique_email):
        register_user(api_client, unique_email)
        resp = api_client.post("/api/auth/login", json={
            "email": unique_email,
            "password": "wrongpass"
        })
        assert resp.status_code == 400

    def test_login_nonexistent_email(self, api_client):
        resp = api_client.post("/api/auth/login", json={
            "email": "nonexistent_xyz@example.com",
            "password": "secret123"
        })
        assert resp.status_code == 400

    def test_login_missing_credentials(self, api_client):
        resp = api_client.post("/api/auth/login", json={})
        assert resp.status_code == 400

    def test_login_missing_password(self, api_client, unique_email):
        register_user(api_client, unique_email)
        resp = api_client.post("/api/auth/login", json={"email": unique_email})
        assert resp.status_code == 400

    def test_login_missing_email(self, api_client):
        resp = api_client.post("/api/auth/login", json={"password": "secret123"})
        assert resp.status_code == 400

    def test_login_invalid_email_format(self, api_client):
        resp = api_client.post("/api/auth/login", json={
            "email": "not-an-email",
            "password": "secret123"
        })
        assert resp.status_code == 400

    def test_login_case_sensitive_password(self, api_client, unique_email):
        register_user(api_client, unique_email)
        resp = api_client.post("/api/auth/login", json={
            "email": unique_email,
            "password": "SECRET123"
        })
        assert resp.status_code == 400

    def test_token_can_be_used_for_auth(self, api_client, unique_email):
        _, token = register_user(api_client, unique_email)
        headers = {"Authorization": f"Bearer {token}"}
        resp = api_client.get("/api/users/me", headers=headers)
        assert resp.status_code == 200

    def test_invalid_token_rejected(self, api_client):
        headers = {"Authorization": "Bearer invalid.token.here"}
        resp = api_client.get("/api/users/me", headers=headers)
        assert resp.status_code == 401

    def test_no_token_rejected(self, api_client):
        resp = api_client.get("/api/users/me")
        assert resp.status_code == 401

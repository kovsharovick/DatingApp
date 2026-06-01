import pytest
import requests
import time

BASE_URL = "http://localhost:8080"


@pytest.fixture(scope="session")
def base_url():
    return BASE_URL


@pytest.fixture(scope="session")
def api_client():
    class Client:
        def __init__(self, base_url):
            self.base_url = base_url
            self.headers = {"Content-Type": "application/json"}

        def post(self, path, json=None, headers=None):
            url = self.base_url + path
            h = {**self.headers, **(headers or {})}
            return requests.post(url, json=json, headers=h)

        def get(self, path, headers=None):
            url = self.base_url + path
            return requests.get(url, headers=headers or self.headers)

        def put(self, path, json=None, headers=None):
            url = self.base_url + path
            h = {**self.headers, **(headers or {})}
            return requests.put(url, json=json, headers=h)

        def delete(self, path, headers=None):
            url = self.base_url + path
            return requests.delete(url, headers=headers or self.headers)

        def post_file(self, path, files, headers=None):
            url = self.base_url + path
            if headers is None:
                headers = {}
            headers = {k: v for k, v in headers.items() if k.lower() != "content-type"}
            return requests.post(url, files=files, headers=headers)

    return Client(BASE_URL)


@pytest.fixture
def unique_email():
    return f"test_{int(time.time() * 1000)}@example.com"


@pytest.fixture
def registered_user(api_client, unique_email):
    payload = {
        "email": unique_email,
        "password": "secret123",
        "name": "TestUser",
        "dateOfBirth": "1990-01-01",
        "gender": "FEMALE",
        "city": "Москва"
    }
    resp = api_client.post("/api/auth/register", json=payload)
    assert resp.status_code == 201, f"Registration failed: {resp.text}"
    data = resp.json()
    return data["userId"], data["token"]


@pytest.fixture
def user_token(registered_user):
    return registered_user[1]


@pytest.fixture
def user_id(registered_user):
    return registered_user[0]


@pytest.fixture
def auth_headers(user_token):
    return {"Authorization": f"Bearer {user_token}"}


@pytest.fixture
def second_user(api_client):
    email = f"user2_{int(time.time() * 1000)}@example.com"
    payload = {
        "email": email,
        "password": "secret123",
        "name": "SecondUser",
        "dateOfBirth": "1992-05-10",
        "gender": "MALE",
        "city": "Москва"
    }
    resp = api_client.post("/api/auth/register", json=payload)
    assert resp.status_code == 201, f"Second user registration failed: {resp.text}"
    data = resp.json()
    return data["userId"], data["token"]


@pytest.fixture
def second_user_id(second_user):
    return second_user[0]


@pytest.fixture
def second_user_token(second_user):
    return second_user[1]


@pytest.fixture
def second_auth_headers(second_user_token):
    return {"Authorization": f"Bearer {second_user_token}"}


@pytest.fixture
def third_user(api_client):
    """A third registered user."""
    email = f"user3_{int(time.time() * 1000)}@example.com"
    payload = {
        "email": email,
        "password": "secret123",
        "name": "ThirdUser",
        "dateOfBirth": "1993-03-20",
        "gender": "FEMALE",
        "city": "Москва"
    }
    resp = api_client.post("/api/auth/register", json=payload)
    assert resp.status_code == 201
    data = resp.json()
    return data["userId"], data["token"]


@pytest.fixture
def third_user_id(third_user):
    return third_user[0]


@pytest.fixture
def third_auth_headers(third_user):
    return {"Authorization": f"Bearer {third_user[1]}"}


def make_match(api_client, user1_id, user1_token, user2_id, user2_token):
    h1 = {"Authorization": f"Bearer {user1_token}"}
    h2 = {"Authorization": f"Bearer {user2_token}"}
    api_client.post("/api/swipes", json={"targetUserId": user2_id, "direction": "LIKE"}, headers=h1)
    resp = api_client.post("/api/swipes", json={"targetUserId": user1_id, "direction": "LIKE"}, headers=h2)
    matched = resp.json().get("data", False)
    if matched:
        matches = api_client.get("/api/matches", headers=h1).json()
        for m in matches:
            if m["partnerId"] == user2_id:
                return m["matchId"]
    return None

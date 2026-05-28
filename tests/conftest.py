import pytest
import requests

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
    import time
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
def auth_headers(user_token):
    return {"Authorization": f"Bearer {user_token}"}

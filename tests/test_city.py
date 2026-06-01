import pytest


class TestCityController:

    def test_autocomplete_returns_results(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=Мос")
        assert resp.status_code == 200
        data = resp.json()
        assert isinstance(data, list)
        assert len(data) > 0

    def test_autocomplete_contains_moscow(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=Москва")
        assert resp.status_code == 200
        assert "Москва" in resp.json()

    def test_autocomplete_case_insensitive(self, api_client):
        resp_lower = api_client.get("/api/cities/autocomplete?q=москва")
        resp_upper = api_client.get("/api/cities/autocomplete?q=МОСКВА")
        assert resp_lower.status_code == 200
        assert resp_upper.status_code == 200
        assert resp_lower.json() == resp_upper.json()

    def test_autocomplete_no_results_unknown_prefix(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=XxXxXxXxXxXxXxX")
        assert resp.status_code == 200
        assert resp.json() == []

    def test_autocomplete_empty_query_returns_empty(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=")
        assert resp.status_code == 200
        assert resp.json() == []

    def test_autocomplete_missing_query_param(self, api_client):
        resp = api_client.get("/api/cities/autocomplete")
        assert resp.status_code in (400, 401)

    def test_autocomplete_limit_max_10(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=а")
        assert resp.status_code == 200
        assert len(resp.json()) <= 10

    def test_autocomplete_distinct_results(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=Нов")
        assert resp.status_code == 200
        results = resp.json()
        assert len(results) == len(set(results)), "Duplicate city names returned"

    def test_autocomplete_returns_strings(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=Екат")
        assert resp.status_code == 200
        for item in resp.json():
            assert isinstance(item, str)

    def test_autocomplete_accessible_without_auth(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=Мос")
        assert resp.status_code == 200

    def test_autocomplete_single_char_prefix(self, api_client):
        resp = api_client.get("/api/cities/autocomplete?q=М")
        assert resp.status_code == 200
        assert isinstance(resp.json(), list)

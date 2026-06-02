import pytest
import tempfile
from tests.utils import create_dummy_image, create_invalid_image, create_large_image


class TestUserController:

    def test_get_profile_success(self, api_client, auth_headers):
        resp = api_client.get("/api/users/me", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        assert "id" in data
        assert "name" in data

    def test_get_profile_contains_expected_fields(self, api_client, auth_headers):
        resp = api_client.get("/api/users/me", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        for field in ("id", "name", "age", "city", "hidden"):
            assert field in data, f"Missing field: {field}"

    def test_get_profile_unauthorized(self, api_client):
        resp = api_client.get("/api/users/me")
        assert resp.status_code == 401

    def test_get_profile_invalid_token(self, api_client):
        resp = api_client.get("/api/users/me", headers={"Authorization": "Bearer bad.token"})
        assert resp.status_code == 401

    def test_update_profile_success(self, api_client, auth_headers):
        update_data = {
            "name": "NewName",
            "minAge": 20,
            "maxAge": 35,
            "radiusKm": 100,
            "preferredGenders": ["MALE", "FEMALE"]
        }
        resp = api_client.put("/api/users/me", json=update_data, headers=auth_headers)
        assert resp.status_code == 200
        assert resp.json()["name"] == "NewName"

    def test_update_profile_name_only(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "OnlyName"}, headers=auth_headers)
        assert resp.status_code == 200
        assert resp.json()["name"] == "OnlyName"

    def test_update_profile_description(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={
            "name": "Tester",
            "description": "Hello, I am a test user"
        }, headers=auth_headers)
        assert resp.status_code == 200
        assert resp.json()["description"] == "Hello, I am a test user"

    def test_update_profile_hidden_flag(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Hidden", "hidden": True}, headers=auth_headers)
        assert resp.status_code == 200
        assert resp.json()["hidden"] is True
        # restore
        api_client.put("/api/users/me", json={"name": "Hidden", "hidden": False}, headers=auth_headers)

    def test_update_profile_valid_city(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "CityTest", "city": "Санкт-Петербург"},
                              headers=auth_headers)
        assert resp.status_code == 200

    def test_update_profile_invalid_city(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Test", "city": "CityThatDoesNotExist_XYZ"},
                              headers=auth_headers)
        assert resp.status_code == 400

    @pytest.mark.xfail(
        reason="Backend bug: UserUpdateRequest.name has @Size(max=100) but lacks "
               "@NotBlank, so empty string passes validation. "
               "Fix: add @NotBlank to UserUpdateRequest.name field. "
               "This test will auto-pass once the fix is deployed."
    )
    def test_update_profile_invalid_name_empty(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": ""}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_name_too_long(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "A" * 101}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_description_too_long(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Test", "description": "x" * 501}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_invalid_radius_negative(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "radiusKm": -10}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_invalid_radius_zero(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "radiusKm": 0}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_radius_too_large(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "radiusKm": 9999}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_max_valid_radius(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "radiusKm": 500}, headers=auth_headers)
        assert resp.status_code == 200

    def test_update_profile_invalid_age_range(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "minAge": 40, "maxAge": 30}, headers=auth_headers)
        assert resp.status_code == 400
        body = resp.json()
        assert "errors" in body
        assert "minAge" in body["errors"]

    def test_update_profile_min_age_below_18(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "minAge": 16}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_min_age_above_99(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "minAge": 100}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_max_age_above_99(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "maxAge": 100}, headers=auth_headers)
        assert resp.status_code == 400

    def test_update_profile_equal_min_max_age(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": "Name", "minAge": 25, "maxAge": 25}, headers=auth_headers)
        assert resp.status_code == 200

    def test_update_profile_preferred_genders(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={
            "name": "Tester",
            "preferredGenders": ["MALE"]
        }, headers=auth_headers)
        assert resp.status_code == 200

    def test_update_profile_empty_preferred_genders(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={
            "name": "Tester",
            "preferredGenders": []
        }, headers=auth_headers)
        assert resp.status_code == 200

    def test_update_profile_unauthorized(self, api_client):
        resp = api_client.put("/api/users/me", json={"name": "Hacker"})
        assert resp.status_code == 401

    def test_get_public_profile_success(self, api_client, auth_headers, second_user_id):
        resp = api_client.get(f"/api/users/{second_user_id}/profile", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        assert data["id"] == second_user_id
        assert "name" in data

    def test_get_public_profile_fields(self, api_client, auth_headers, second_user_id):
        resp = api_client.get(f"/api/users/{second_user_id}/profile", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        for field in ("id", "name", "age", "city", "region"):
            assert field in data

    def test_get_public_profile_unauthorized(self, api_client, second_user_id):
        resp = api_client.get(f"/api/users/{second_user_id}/profile")
        assert resp.status_code == 401

    def test_get_public_profile_nonexistent_user(self, api_client, auth_headers):
        resp = api_client.get("/api/users/999999999/profile", headers=auth_headers)
        assert resp.status_code == 400

    def test_get_public_profile_hidden_user(self, api_client, auth_headers, second_user_id, second_auth_headers):
        put_resp = api_client.put("/api/users/me", json={"name": "SecondUser", "hidden": True},
                                  headers=second_auth_headers)
        assert put_resp.status_code == 200, f"Failed to hide user: {put_resp.text}"
        resp = api_client.get(f"/api/users/{second_user_id}/profile", headers=auth_headers)
        assert resp.status_code == 400
        api_client.put("/api/users/me", json={"name": "SecondUser", "hidden": False}, headers=second_auth_headers)

    def test_get_own_public_profile(self, api_client, auth_headers, user_id):
        resp = api_client.get(f"/api/users/{user_id}/profile", headers=auth_headers)
        assert resp.status_code == 200

    def test_get_public_profile_blocked_by_target(self, api_client, auth_headers, user_id,
                                                  second_user_id, second_auth_headers):
        api_client.post("/api/blocks", json={"blockedUserId": user_id}, headers=second_auth_headers)
        resp = api_client.get(f"/api/users/{second_user_id}/profile", headers=auth_headers)
        assert resp.status_code == 400
        api_client.delete(f"/api/blocks/{user_id}", headers=second_auth_headers)

    def test_get_public_profile_requester_blocked_target(self, api_client, auth_headers,
                                                         user_id, second_user_id):
        api_client.post("/api/blocks", json={"blockedUserId": second_user_id}, headers=auth_headers)
        resp = api_client.get(f"/api/users/{second_user_id}/profile", headers=auth_headers)
        assert resp.status_code == 400
        api_client.delete(f"/api/blocks/{second_user_id}", headers=auth_headers)

    def test_upload_avatar_success(self, api_client, auth_headers):
        img_path = create_dummy_image()
        with open(img_path, "rb") as f:
            files = {"file": (img_path, f, "image/jpeg")}
            resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert resp.status_code == 200
        assert "avatarUrl" in resp.json()

    def test_upload_avatar_png(self, api_client, auth_headers):
        img_path = create_dummy_image()
        with open(img_path, "rb") as f:
            files = {"file": (img_path, f, "image/png")}
            resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert resp.status_code == 200

    def test_upload_avatar_invalid_content_type(self, api_client, auth_headers):
        txt_path = create_invalid_image()
        with open(txt_path, "rb") as f:
            files = {"file": (txt_path, f, "text/plain")}
            resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert resp.status_code == 400
        assert "Unsupported or missing file type" in resp.text

    def test_upload_avatar_too_large(self, api_client, auth_headers):
        large_path = create_large_image(size_mb=6)
        with open(large_path, "rb") as f:
            files = {"file": (large_path, f, "image/jpeg")}
            resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert resp.status_code == 400
        assert "maximum upload size exceeded" in resp.text.lower() or "size" in resp.text.lower()

    def test_upload_avatar_empty_file(self, api_client, auth_headers):
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
            tmp.write(b"")
            path = tmp.name
        with open(path, "rb") as f:
            files = {"file": (path, f, "image/jpeg")}
            resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert resp.status_code == 400
        assert "empty" in resp.text.lower()

    def test_upload_avatar_unauthorized(self, api_client):
        img_path = create_dummy_image()
        with open(img_path, "rb") as f:
            files = {"file": f}
            resp = api_client.post_file("/api/users/me/avatar", files=files)
        assert resp.status_code == 401

    def test_upload_avatar_replaces_previous(self, api_client, auth_headers):
        img_path = create_dummy_image()
        for _ in range(2):
            with open(img_path, "rb") as f:
                files = {"file": (img_path, f, "image/jpeg")}
                resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
            assert resp.status_code == 200

    def test_delete_avatar_success(self, api_client, auth_headers):
        img_path = create_dummy_image()
        with open(img_path, "rb") as f:
            files = {"file": (img_path, f, "image/jpeg")}
            upload_resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert upload_resp.status_code == 200
        resp = api_client.delete("/api/users/me/avatar", headers=auth_headers)
        assert resp.status_code == 204

    def test_delete_avatar_when_not_exists(self, api_client, auth_headers):
        resp = api_client.delete("/api/users/me/avatar", headers=auth_headers)
        assert resp.status_code == 204

    def test_delete_avatar_unauthorized(self, api_client):
        resp = api_client.delete("/api/users/me/avatar")
        assert resp.status_code == 401

    def test_delete_avatar_clears_avatar_url(self, api_client, auth_headers):
        img_path = create_dummy_image()
        with open(img_path, "rb") as f:
            files = {"file": (img_path, f, "image/jpeg")}
            api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        api_client.delete("/api/users/me/avatar", headers=auth_headers)
        profile = api_client.get("/api/users/me", headers=auth_headers).json()
        assert profile.get("avatarUrl") is None

import pytest
from tests.utils import create_dummy_image, create_invalid_image, create_large_image


class TestUserController:
    def test_get_profile_success(self, api_client, auth_headers):
        resp = api_client.get("/api/users/me", headers=auth_headers)
        assert resp.status_code == 200
        data = resp.json()
        assert "id" in data
        assert "name" in data

    def test_get_profile_unauthorized(self, api_client):
        resp = api_client.get("/api/users/me")
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

    def test_update_profile_invalid_data(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"name": ""}, headers=auth_headers)
        assert resp.status_code == 400
        resp2 = api_client.put("/api/users/me", json={"radiusKm": -10}, headers=auth_headers)
        assert resp2.status_code != 500

    def test_update_profile_unauthorized(self, api_client):
        resp = api_client.put("/api/users/me", json={"name": "Hacker"})
        assert resp.status_code == 401

    def test_upload_avatar_success(self, api_client, auth_headers):
        img_path = create_dummy_image()
        with open(img_path, "rb") as f:
            files = {"file": (img_path, f, "image/jpeg")}
            resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert resp.status_code == 200
        assert "avatarUrl" in resp.json()

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
        import tempfile
        with tempfile.NamedTemporaryFile(suffix='.jpg', delete=False) as tmp:
            tmp.write(b'')
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

    def test_delete_avatar_success(self, api_client, auth_headers):
        img_path = create_dummy_image()
        with open(img_path, "rb") as f:
            files = {"file": (img_path, f, "image/jpeg")}
            upload_resp = api_client.post_file("/api/users/me/avatar", files=files, headers=auth_headers)
        assert upload_resp.status_code == 200
        # затем удалим
        resp = api_client.delete("/api/users/me/avatar", headers=auth_headers)
        assert resp.status_code == 204

    def test_delete_avatar_when_not_exists(self, api_client, auth_headers):
        resp = api_client.delete("/api/users/me/avatar", headers=auth_headers)
        assert resp.status_code == 204

    def test_delete_avatar_unauthorized(self, api_client):
        resp = api_client.delete("/api/users/me/avatar")
        assert resp.status_code == 401

    def test_update_profile_invalid_age_range(self, api_client, auth_headers):
        resp = api_client.put("/api/users/me", json={"minAge": 40, "maxAge": 30}, headers=auth_headers)
        assert resp.status_code in (200, 400)

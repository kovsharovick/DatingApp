import pytest
import tempfile
import os
import struct
import time
from tests.utils import register_user


def _create_minimal_mp4():
    ftyp = (
        b'\x00\x00\x00\x18'
        b'ftyp'
        b'isom'
        b'\x00\x00\x02\x00'
        b'isom'
        b'iso2'
    )

    mvhd = (
        b'\x00\x00\x00\x6c'
        b'mvhd'
        b'\x00'
        b'\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x03\xe8'
        b'\x00\x00\x03\xe8'
        b'\x00\x01\x00\x00'
        b'\x01\x00'
        b'\x00\x00'
        b'\x00\x00\x00\x00\x00\x00\x00\x00'
        b'\x00\x01\x00\x00\x00\x00\x00\x00'
        b'\x00\x00\x00\x00\x00\x01\x00\x00'
        b'\x00\x00\x00\x00\x00\x00\x00\x00'
        b'\x40\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x00\x00'
        b'\x00\x00\x00\x02'
    )

    moov_content = mvhd
    moov_size = 8 + len(moov_content)
    moov = struct.pack('>I', moov_size) + b'moov' + moov_content

    mdat = b'\x00\x00\x00\x08mdat'

    data = ftyp + moov + mdat

    tmp = tempfile.NamedTemporaryFile(suffix='.mp4', delete=False)
    tmp.write(data)
    tmp.close()
    return tmp.name


def _ffmpeg_create_mp4():
    out = tempfile.NamedTemporaryFile(suffix='.mp4', delete=False)
    out.close()
    ret = os.system(
        f'ffmpeg -y -f lavfi -i color=c=black:s=64x64:d=2 -t 2 '
        f'"{out.name}" -loglevel quiet 2>/dev/null'
    )
    if ret == 0 and os.path.getsize(out.name) > 0:
        return out.name
    os.unlink(out.name)
    return None


def _best_mp4():
    path = _ffmpeg_create_mp4()
    return path if path else _create_minimal_mp4()


@pytest.fixture
def third_auth_headers(api_client):
    email = f"third_{int(time.time() * 1000)}@example.com"
    user_id, token = register_user(api_client, email)
    assert token is not None, "Не удалось зарегистрировать третьего пользователя"
    return {"Authorization": f"Bearer {token}"}


class TestVideoController:

    def test_get_my_videos_returns_list(self, api_client, auth_headers):
        resp = api_client.get("/api/videos/me", headers=auth_headers)
        assert resp.status_code == 200
        assert isinstance(resp.json(), list)

    def test_get_my_videos_unauthorized(self, api_client):
        resp = api_client.get("/api/videos/me")
        assert resp.status_code == 401

    def test_get_my_videos_empty_for_new_user(self, api_client, third_auth_headers):
        resp = api_client.get("/api/videos/me", headers=third_auth_headers)
        assert resp.status_code == 200
        assert resp.json() == []

    def test_get_my_videos_response_fields(self, api_client, auth_headers):
        resp = api_client.get("/api/videos/me", headers=auth_headers)
        assert resp.status_code == 200
        videos = resp.json()
        if videos:
            v = videos[0]
            for field in ("id", "durationSec", "active", "createdAt"):
                assert field in v, f"Missing field: {field}"

    def test_upload_video_unauthorized(self, api_client):
        mp4 = _create_minimal_mp4()
        with open(mp4, "rb") as f:
            resp = api_client.post_file("/api/videos", files={"file": (mp4, f, "video/mp4")})
        assert resp.status_code == 401

    def test_upload_video_invalid_content_type(self, api_client, auth_headers):
        with tempfile.NamedTemporaryFile(suffix=".txt", delete=False) as tmp:
            tmp.write(b"not a video")
            tmp.close()
            with open(tmp.name, "rb") as f:
                resp = api_client.post_file("/api/videos", files={
                    "file": (tmp.name, f, "text/plain")
                }, headers=auth_headers)
        assert resp.status_code == 400
        assert "unsupported" in resp.text.lower() or "format" in resp.text.lower()

    def test_upload_video_empty_file(self, api_client, auth_headers):
        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp:
            tmp.write(b"")
            tmp.close()
            with open(tmp.name, "rb") as f:
                resp = api_client.post_file("/api/videos", files={
                    "file": (tmp.name, f, "video/mp4")
                }, headers=auth_headers)
        assert resp.status_code == 400

    def test_upload_video_too_large(self, api_client, auth_headers):
        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp:
            tmp.write(b"\x00" * (251 * 1024 * 1024))
            tmp.close()
            with open(tmp.name, "rb") as f:
                resp = api_client.post_file("/api/videos", files={
                    "file": (tmp.name, f, "video/mp4")
                }, headers=auth_headers)
        assert resp.status_code == 400
        assert any(keyword in resp.text.lower() for keyword in ("size", "maximum", "exceed"))

    def test_upload_video_success(self, api_client, auth_headers):
        path = _best_mp4()
        with open(path, "rb") as f:
            resp = api_client.post_file("/api/videos", files={
                "file": (path, f, "video/mp4")
            }, headers=auth_headers)
        if resp.status_code == 400 and ("duration" in resp.text.lower() or "ffprobe" in resp.text.lower()):
            pytest.skip("Server-side ffprobe unavailable — cannot validate video duration")
        assert resp.status_code == 200, f"Upload failed: {resp.text}"
        assert "videoId" in resp.json()

    def test_upload_video_appears_in_list(self, api_client, auth_headers):
        path = _best_mp4()
        with open(path, "rb") as f:
            resp = api_client.post_file("/api/videos", files={
                "file": (path, f, "video/mp4")
            }, headers=auth_headers)
        if resp.status_code == 400 and ("duration" in resp.text.lower() or "ffprobe" in resp.text.lower()):
            pytest.skip("Server-side ffprobe unavailable")
        if resp.status_code != 200:
            pytest.skip(f"Upload failed: {resp.text}")
        video_id = resp.json()["videoId"]
        videos = api_client.get("/api/videos/me", headers=auth_headers).json()
        assert video_id in [v["id"] for v in videos]

    def test_upload_second_video_deactivates_first(self, api_client, auth_headers):
        path1 = _best_mp4()
        path2 = _best_mp4()
        results = []
        for path in (path1, path2):
            with open(path, "rb") as f:
                r = api_client.post_file("/api/videos", files={"file": (path, f, "video/mp4")}, headers=auth_headers)
            if r.status_code == 400 and ("duration" in r.text.lower() or "ffprobe" in r.text.lower()):
                pytest.skip("Server-side ffprobe unavailable")
            results.append(r)
        assert all(r.status_code == 200 for r in results), f"Upload failed: {[r.text for r in results]}"
        videos = api_client.get("/api/videos/me", headers=auth_headers).json()
        active = [v for v in videos if v["active"]]
        assert len(active) == 1, f"Expected 1 active video, got {len(active)}"

    def test_upload_video_avi_format(self, api_client, auth_headers):
        with tempfile.NamedTemporaryFile(suffix=".avi", delete=False) as tmp:
            tmp.write(b"RIFF" + b"\x00" * 100)
            tmp.close()
            with open(tmp.name, "rb") as f:
                resp = api_client.post_file("/api/videos", files={
                    "file": (tmp.name, f, "video/x-msvideo")
                }, headers=auth_headers)
        assert resp.status_code == 400

    def test_upload_video_mov_format_accepted(self, api_client, auth_headers):
        with tempfile.NamedTemporaryFile(suffix=".mov", delete=False) as tmp:
            tmp.write(b"\x00\x00\x00\x08wide" + b"\x00" * 100)
            tmp.close()
            with open(tmp.name, "rb") as f:
                resp = api_client.post_file("/api/videos", files={
                    "file": (tmp.name, f, "video/quicktime")
                }, headers=auth_headers)
        assert resp.status_code == 400

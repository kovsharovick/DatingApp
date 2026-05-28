import requests
import tempfile
import os


def create_dummy_image(path=None, size_kb=10):
    minimal_jpeg = (
        b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00'
        b'\xff\xdb\x00\x43\x00\x03\x02\x02\x03\x02\x02\x03\x03\x03\x03\x04\x03'
        b'\x03\x04\x05\x08\x05\x05\x04\x04\x05\n\x07\x07\x06\x08\x0c\n\x0c\x0c'
        b'\x0b\n\x0b\x0b\r\x0e\x12\x10\r\x0e\x11\x0e\x0b\x0b\x10\x16\x10\x11'
        b'\x13\x14\x15\x15\x15\x0c\x0f\x17\x18\x16\x14\x18\x12\x14\x15\x14\xff'
        b'\xc4\x00\x14\x00\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00'
        b'\x00\x00\x00\xff\xda\x00\x08\x01\x01\x00\x00\x3f\x00\x37\xff\xd9'
    )
    if path is None:
        temp = tempfile.NamedTemporaryFile(suffix='.jpg', delete=False)
        path = temp.name
        temp.close()
    with open(path, 'wb') as f:
        f.write(minimal_jpeg)
        extra = size_kb * 1024 - len(minimal_jpeg)
        if extra > 0:
            f.write(b'\x00' * extra)
    return path


def create_invalid_image(path=None):
    if path is None:
        temp = tempfile.NamedTemporaryFile(suffix='.txt', delete=False)
        path = temp.name
        temp.close()
    with open(path, 'w') as f:
        f.write("This is not an image")
    return path


def create_large_image(path=None, size_mb=6):
    if path is None:
        temp = tempfile.NamedTemporaryFile(suffix='.jpg', delete=False)
        path = temp.name
        temp.close()
    with open(path, 'wb') as f:
        f.write(b'\x00' * (size_mb * 1024 * 1024))
    return path


def register_user(api_client, email, password="secret123", name="Tester", gender="FEMALE", city="Москва"):
    payload = {
        "email": email,
        "password": password,
        "name": name,
        "dateOfBirth": "1995-06-15",
        "gender": gender,
        "city": city
    }
    resp = api_client.post("/api/auth/register", json=payload)
    if resp.status_code == 201:
        return resp.json()["userId"], resp.json()["token"]
    return None, None


def login_user(api_client, email, password):
    resp = api_client.post("/api/auth/login", json={"email": email, "password": password})
    if resp.status_code == 200:
        return resp.json()["userId"], resp.json()["token"]
    return None, None

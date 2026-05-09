import requests
import json
from datetime import datetime

BASE_URL = "http://localhost:8080"
HEADERS = {"Content-Type": "application/json"}

def print_step(step, response):
    print(f"\n{'='*50}")
    print(f"Шаг: {step}")
    print(f"Статус: {response.status_code}")
    try:
        print(f"Ответ: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    except:
        print(f"Тело: {response.text}")

def main():
    # 1. Успешная регистрация
    email = f"test{datetime.now().strftime('%H%M%S')}@example.com"
    register_data = {
        "email": email,
        "password": "secret123",
        "name": "Alice",
        "dateOfBirth": "2000-01-15",
        "gender": "FEMALE",
        "city": "Воронеж"
    }

    print("\n>>>>>> 1. Успешная регистрация")
    resp = requests.post(f"{BASE_URL}/api/auth/register", json=register_data, headers=HEADERS)
    print_step("Регистрация", resp)
    if resp.status_code != 201:
        print("Ошибка регистрации, останавливаемся")
        return
    token = resp.json()["token"]

    # 2. Логин (успешный)
    print("\n>>>>>> 2. Успешный логин")
    resp = requests.post(f"{BASE_URL}/api/auth/login", json={"email": email, "password": "secret123"}, headers=HEADERS)
    print_step("Логин", resp)
    # Если логин успешен, можно обновить токен, но он тот же
    if resp.status_code == 200:
        token = resp.json()["token"]

    auth_headers = {"Authorization": f"Bearer {token}"}

    # 3. Успешное получение профиля
    print("\n>>>>>> 3. Получение профиля (успешно)")
    resp = requests.get(f"{BASE_URL}/api/users/me", headers=auth_headers)
    print_step("Профиль", resp)

    # 4. Успешное обновление профиля
    print("\n>>>>>> 4. Обновление профиля (успешно)")
    update_data = {
        "name": "Alice Updated",
        "radiusKm": 75,
        "preferredGenders": ["MALE", "FEMALE"]
    }
    resp = requests.put(f"{BASE_URL}/api/users/me", json=update_data, headers={**HEADERS, **auth_headers})
    print_step("Обновление", resp)

    # ========== НЕГАТИВНЫЕ ТЕСТЫ ==========

    # 5. Запрос без токена (ожидается 401)
    print("\n>>>>>> 5. Получение профиля БЕЗ токена (должен быть 401)")
    resp = requests.get(f"{BASE_URL}/api/users/me")
    print_step("Без токена", resp)

    # 6. Обновление без токена (ожидается 401)
    print("\n>>>>>> 6. Обновление профиля БЕЗ токена (должен быть 401)")
    resp = requests.put(f"{BASE_URL}/api/users/me", json={"name": "Hacker"}, headers=HEADERS)
    print_step("Обновление без токена", resp)

    # 7. Передача поддельного токена (ожидается 401)
    print("\n>>>>>> 7. Запрос с поддельным токеном (должен быть 401)")
    fake_headers = {"Authorization": "Bearer fake.token.here"}
    resp = requests.get(f"{BASE_URL}/api/users/me", headers=fake_headers)
    print_step("Поддельный токен", resp)

    # 8. Обновление с невалидными данными (ожидается 400 + список ошибок)
    print("\n>>>>>> 8. Обновление с невалидными данными (пустое имя, слишком большой радиус)")
    bad_update = {
        "name": "",                    # @NotBlank нарушено
        "radiusKm": 1000000            # выходит за разумные пределы, но валидации нет, можно добавить @Max
    }
    resp = requests.put(f"{BASE_URL}/api/users/me", json=bad_update, headers={**HEADERS, **auth_headers})
    print_step("Невалидное обновление", resp)
    # здесь мы увидим ошибку только по name, если @NotBlank сработает, радиус пока не проверяется

    # 9. Регистрация с невалидным email (ожидается 400)
    print("\n>>>>>> 9. Регистрация с невалидным email")
    invalid_reg = {
        "email": "not-an-email",
        "password": "123",
        "name": "Bob",
        "dateOfBirth": "2000-01-15",
        "gender": "MALE",
        "city": "Воронеж"
    }
    resp = requests.post(f"{BASE_URL}/api/auth/register", json=invalid_reg, headers=HEADERS)
    print_step("Невалидный email + короткий пароль", resp)

    # 10. Регистрация с несуществующим городом
    print("\n>>>>>> 10. Регистрация с несуществующим городом")
    bad_city = {
        "email": "bob@example.com",
        "password": "valid123",
        "name": "Bob",
        "dateOfBirth": "2000-01-15",
        "gender": "MALE",
        "city": "НеСуществующийГород"
    }
    resp = requests.post(f"{BASE_URL}/api/auth/register", json=bad_city, headers=HEADERS)
    print_step("Город не найден", resp)

    # 11. Попытка повторной регистрации с тем же email
    print("\n>>>>>> 11. Регистрация с уже занятым email")
    resp = requests.post(f"{BASE_URL}/api/auth/register", json=register_data, headers=HEADERS)
    print_step("Дубликат email", resp)

if __name__ == "__main__":
    main()
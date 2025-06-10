from locust import HttpUser, task, between
import random
import time
from datetime import datetime, timedelta
import json
from json.decoder import JSONDecodeError
import uuid

class BookstoreUser(HttpUser):
    host = "http://localhost:8080"
    wait_time = between(1, 5)
    token = None
    user_id = None
    username = None
    books = []
    orders = []

    def on_start(self):
        self.register()
        if self.login():
            self.get_books()
            self.simulate_initial_activity()

    def register(self):
        self.username = f"user_{uuid.uuid4().hex[:8]}"
        payload = {
            "fullname": f"User {self.username}",
            "username": self.username,
            "email": f"{self.username}@example.com",
            "password": "securePassword123!"
        }
        with self.client.post("/auth/register", json=payload, catch_response=True) as response:
            if response.status_code in [200, 201]:
                return True
            else:
                response.failure(f"Falha no registro: {response.status_code} - {response.text}")
        return False

    def login(self):
        payload = {
            "username": self.username,
            "password": "securePassword123!"
        }
        with self.client.post("/auth/login", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.token = data.get("token")
                    if not self.token:
                        response.failure("Token não encontrado na resposta")
                        return False

                    with self.client.get(f"/auth/id/{self.username}",
                                        headers={"Authorization": f"Bearer {self.token}"},
                                        catch_response=True) as user_response:
                        if user_response.status_code == 200:
                            user_data = user_response.json()
                            self.user_id = user_data.get("id")
                            return True
                        else:
                            user_response.failure(f"Falha ao obter ID: {user_response.status_code}")
                except JSONDecodeError:
                    response.failure("Resposta não é JSON válido")
            else:
                response.failure(f"Falha no login: {response.status_code} - {response.text}")
        return False

    def get_books(self):
        headers = {"Authorization": f"Bearer {self.token}"}
        with self.client.get("/catalogo/books", headers=headers, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    self.books = response.json()
                    return True
                except JSONDecodeError:
                    response.failure("Resposta JSON inválida")
            else:
                response.failure(f"Falha ao obter livros: {response.status_code}")
        return False

    def simulate_initial_activity(self):
        if self.books:
            for _ in range(random.randint(1, 3)):
                book = random.choice(self.books)
                self.add_to_cart(book["id"], random.randint(1, 2))

    @task(10)
    def browse_books(self):
        headers = {"Authorization": f"Bearer {self.token}"}
        self.client.get("/catalogo/books", headers=headers, name="(1) Listar livros")

    @task(7)
    def add_to_cart_task(self):
        if not self.books:
            return

        book = random.choice(self.books)
        quantity = random.randint(1, 3)
        self.add_to_cart(book["id"], quantity)

    def add_to_cart(self, book_id, quantity):
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

        book = next((b for b in self.books if b["id"] == book_id), None)
        if not book:
            return False

        payload = {
            "userId": self.user_id,
            "username": self.username,
            "bookId": book_id,
            "quantity": quantity,
            "unitPrice": book["price"],
            "subTotal": book["price"] * quantity
        }

        with self.client.post(
            "/cart/cartitem/add",
            json=payload,
            headers=headers,
            name="(2) Adicionar ao carrinho",
            catch_response=True
        ) as response:
            # Aceitar tanto 200 quanto 201 como sucesso
            if response.status_code in [200, 201]:
                return True
            else:
                response.failure(f"Falha ao adicionar ao carrinho: {response.status_code} - {response.text}")
        return False

    @task(3)
    def view_cart(self):
        headers = {"Authorization": f"Bearer {self.token}"}
        with self.client.get(
            f"/cart/cartitem/user/{self.user_id}",
            headers=headers,
            name="(3) Ver carrinho",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    response.json()
                except JSONDecodeError:
                    response.failure("Resposta JSON inválida")
            else:
                response.failure(f"Falha ao ver carrinho: {response.status_code}")

    @task(2)
    def checkout_process(self):
        headers = {"Authorization": f"Bearer {self.token}"}

        # Verificar se o carrinho tem itens
        with self.client.get(
            f"/cart/cartitem/user/{self.user_id}",
            headers=headers,
            catch_response=True
        ) as cart_response:
            if cart_response.status_code != 200:
                return

            try:
                cart_items = cart_response.json()
                if not cart_items:
                    return
            except:
                return

        shipping_details = {
            "firstName": f"FirstName{random.randint(1, 100)}",
            "lastName": f"LastName{random.randint(1, 100)}",
            "address": f"Rua {random.randint(1, 100)}, nº {random.randint(1, 500)}",
            "city": random.choice(["Lisboa", "Porto", "Braga", "Coimbra"]),
            "email": f"{self.username}@example.com",
            "postal_code": f"{random.randint(1000, 4999)}-{random.randint(100, 999)}"
        }

        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

        with self.client.post(
            f"/checkout/{self.user_id}",
            json=shipping_details,
            headers=headers,
            name="(4) Finalizar compra",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                saga_id = response.text.split("ID:")[-1].strip()
                self.monitor_saga_status(saga_id)
            else:
                response.failure(f"Falha no checkout: {response.status_code} - {response.text}")

    def monitor_saga_status(self, saga_id):
        headers = {"Authorization": f"Bearer {self.token}"}
        for _ in range(5):  # Reduzir o número de tentativas
            self.wait()  # Usar o sistema de espera do Locust
            with self.client.get(
                f"/checkout/saga/status/{saga_id}",
                headers=headers,
                name="(5) Verificar status saga",
                catch_response=True
            ) as response:
                if response.status_code == 200:
                    status = response.text
                    if status == "SUCCESS":
                        return True
                    elif status == "FAILED":
                        return False
                else:
                    response.failure(f"Falha ao verificar status: {response.status_code}")
        return False

    @task(4)
    def view_orders(self):
        end_date = datetime.now()
        start_date = end_date - timedelta(days=random.randint(1, 30))

        headers = {"Authorization": f"Bearer {self.token}"}
        params = {
            "startDate": start_date.strftime("%Y-%m-%d"),
            "endDate": end_date.strftime("%Y-%m-%d")
        }

        with self.client.get(
            f"/query/orders/{self.user_id}",
            params=params,
            headers=headers,
            name="(6) Ver pedidos",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    self.orders = response.json()
                except JSONDecodeError:
                    response.failure("Resposta JSON inválida")
            else:
                response.failure(f"Falha ao visualizar pedidos: {response.status_code}")

    @task(2)
    def view_order_details(self):
        if not self.orders:
            return

        order = random.choice(self.orders)
        headers = {"Authorization": f"Bearer {self.token}"}
        with self.client.get(
            f"/gateway/orderDetails/{order['orderId']}",
            headers=headers,
            name="(7) Ver detalhes pedido",
            catch_response=True
        ) as response:
            if response.status_code != 200:
                response.failure(f"Falha ao obter detalhes: {response.status_code}")

    @task(1)
    def remove_from_cart(self):
        headers = {"Authorization": f"Bearer {self.token}"}

        # Obter carrinho atual
        with self.client.get(
            f"/cart/cartitem/user/{self.user_id}",
            headers=headers,
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    server_cart = response.json()
                    if server_cart:
                        item_to_remove = random.choice(server_cart)
                        self.client.delete(
                            f"/cart/cartitem/delete/{item_to_remove['id']}",
                            headers=headers,
                            name="(8) Remover do carrinho"
                        )
                except (JSONDecodeError, KeyError):
                    pass
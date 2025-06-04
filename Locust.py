from locust import HttpUser, task, between
import random
import time
from datetime import datetime, timedelta
import json
from json.decoder import JSONDecodeError

class MicroservicesUser(HttpUser):
    host = "http://localhost:8080"  # Definindo o host base do gateway
    wait_time = between(1, 3)
    token = None
    user_id = None
    book_ids = []
    cart_items = []
    order_ids = []

    def on_start(self):
        """Executado quando um usuário virtual inicia"""
        self.register()
        self.login()
        self.get_books()

    def register(self):
        """Registra um novo usuário"""
        user_id = random.randint(10000, 99999)
        payload = {
            "fullname": f"User {user_id}",
            "username": f"user_{user_id}",
            "email": f"user_{user_id}@example.com",
            "password": "password123"
        }
        with self.client.post("/auth/register", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                self.user_id = user_id
                return True
            else:
                response.failure(f"Falha no registro: {response.status_code}")
        return False

    def login(self):
        """Faz login e obtém o token JWT"""
        payload = {
            "username": f"user_{self.user_id}",
            "password": "password123"
        }
        with self.client.post("/auth/login", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.token = data.get("token")
                    if not self.token:
                        response.failure("Token não encontrado na resposta")
                        return False
                    return True
                except JSONDecodeError:
                    response.failure("Resposta não é JSON válido")
            else:
                response.failure(f"Falha no login: {response.status_code}")
        return False

    def get_books(self):
        """Obtém a lista de livros disponíveis"""
        headers = {"Authorization": f"Bearer {self.token}"}
        with self.client.get("/catalogo/books", headers=headers, catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"Falha ao obter livros: {response.status_code}")
                return False

            try:
                books = response.json()
                self.book_ids = [book["id"] for book in books]  # Corrigido para "id"
                return True
            except KeyError:
                response.failure("Campo 'id' não encontrado na resposta")
            except JSONDecodeError:
                response.failure("Resposta JSON inválida")
        return False

    @task(5)
    def view_books(self):
        """Visualiza a lista de livros"""
        headers = {"Authorization": f"Bearer {self.token}"}
        with self.client.get("/catalogo/books", headers=headers, catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"Falha ao visualizar livros: {response.status_code}")

    @task(3)
    def add_to_cart(self):
        """Adiciona um livro ao carrinho"""
        if not self.book_ids:
            return

        book_id = random.choice(self.book_ids)
        quantity = random.randint(1, 3)

        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
        payload = {
            "bookId": str(book_id),
            "quantity": quantity
        }

        with self.client.post(
            "/gateway/addBookToCart",
            params=payload,
            headers=headers,
            catch_response=True
        ) as response:
            if response.status_code == 200:
                self.cart_items.append(book_id)
                return True
            else:
                response.failure(f"Falha ao adicionar ao carrinho: {response.status_code}")
        return False

    @task(1)
    def checkout(self):
        """Finaliza a compra"""
        if not self.cart_items:
            return

        headers = {"Authorization": f"Bearer {self.token}"}
        with self.client.post(
            f"/checkout/{self.user_id}",
            headers=headers,
            catch_response=True
        ) as response:
            if response.status_code == 200:
                # O serviço retorna uma mensagem de texto, não JSON
                self.cart_items = []
                return True
            else:
                response.failure(f"Falha no checkout: {response.status_code}")
        return False

    @task(2)
    def view_orders(self):
        """Visualiza encomendas entre duas datas"""
        end_date = datetime.now()
        start_date = end_date - timedelta(days=30)

        headers = {"Authorization": f"Bearer {self.token}"}
        params = {
            "startDate": start_date.strftime("%Y-%m-%d"),
            "endDate": end_date.strftime("%Y-%m-%d")
        }

        with self.client.get(
            f"/query/orders/{self.user_id}",
            params=params,
            headers=headers,
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    orders = response.json()
                    # Atualiza a lista de order_ids com os pedidos obtidos
                    self.order_ids = [order["orderId"] for order in orders]
                except (KeyError, JSONDecodeError):
                    response.failure("Falha ao processar pedidos")
            else:
                response.failure(f"Falha ao visualizar pedidos: {response.status_code}")

    @task(1)
    def view_order_details(self):
        """Visualiza detalhes de uma encomenda específica"""
        if not self.order_ids:
            return

        order_id = random.choice(self.order_ids)
        headers = {"Authorization": f"Bearer {self.token}"}
        with self.client.get(
            f"/gateway/orderDetails/{order_id}",
            headers=headers,
            catch_response=True
        ) as response:
            if response.status_code != 200:
                response.failure(f"Falha ao obter detalhes: {response.status_code}")
#Criar a bookstore
#drop database if exists bookstore;
#CREATE DATABASE bookstore;

#Utilizar a bookstore
#use bookstore;

# Ver o que está nas tabelas:
#select * from book;
#select * from author;
#select * from user;
#select * from cart;
#select * from cart_item;
#select * from shipping_order;
#select * from orders;
#select * from order_details;
#select * from category;
#select * from subcategory;

#Inserir na bookstore
#INSERT INTO user (
#  fullname,
#  username,
#  password,
#  email
#) VALUES (
#  'João Silva',
#  'joaosilva',
#  '$2a$10$h7KOBqEtrB/eKgnPQARpSOLBmZcXeX/IAtDYvJFoFSdG1rdSbOeoO',
#  'joao@email.com'
#);

#INSERT INTO category (name) VALUES ('Ficção');

#INSERT INTO subcategory (name, category_id) VALUES ('Fantasia', 1);

#INSERT INTO author (author_name) VALUES ('J.K. Rowling');

#INSERT INTO book (
#  title,
#  isbn_number,
#  image,
#  description,
#  price,
#  quantity,
#  author_id,
#  category_id,
#  subcategory_id
#) VALUES (
#  'Harry Potter e a Pedra Filosofal',
#  '9789722325961',
#  NULL,
#  'Livro de fantasia e aventura.',
#  14.99,
#  10,
#  1,
#  1,
#  1
#);

#INSERT INTO book (
#  title,
#  isbn_number,
#  image,
#  description,
#  price,
#  quantity,
#  author_id,
#  category_id,
#  subcategory_id
#) VALUES (
#  'Harry Potter e a Câmara dos Segredos',
#  '9789722327331',
#  NULL,
#  'Segundo volume da saga Harry Potter.',
#  16.50,
#  8,
#  1,
#  1,
#  1
#);

#Inserir algo na shipping order:
#INSERT INTO shipping_order (first_name, last_name, address, city, email, postal_code)
#VALUES ('João', 'Silva', 'Rua dos Livros, 123', 'Lisboa', 'joao@email.com', '1000-001');

#Inserir em orders;
#INSERT INTO orders (order_date, total_price)
#VALUES (NOW(), 29.98);

#Inserir em order_details:
#INSERT INTO order_details (quantity, sub_total, book_id, shippingorder_id, user_id)
#VALUES
#(1, 14.99, 1, 1, 1);

#Para testar as compositions
#SELECT * FROM order_details WHERE order_detailsid = 1;
#SELECT * FROM shipping_order WHERE shippingorder_id = 1;
#SELECT * FROM order_details;
#SHOW COLUMNS FROM shipping_order;
#show columns from user;


#=======================TRANSIÇÃO PARA MICROSERVIÇOS=========================
#=========================SERVIÇO DE AUTENTICAÇÃO============================
DROP DATABASE IF EXISTS authdb;
CREATE DATABASE authdb;
USE authdb;
show tables from authdb;
select * from authdb.user;

#CREATE TABLE user (
#    userid BIGINT PRIMARY KEY AUTO_INCREMENT,
#    fullname VARCHAR(255),
#    username VARCHAR(255) UNIQUE,
#    password VARCHAR(255),
#    email VARCHAR(255) UNIQUE
#);

INSERT INTO user (fullname, username, password, email)
VALUES ('João Pereira Silva', 'joaosilva', '$2a$10$h7KOBqEtrB/eKgnPQARpSOLBmZcXeX/IAtDYvJFoFSdG1rdSbOeoO', 'joao@email.com');

select * from authdb.user;

#=========================SERVIÇO DE CATÁLOGO=============================
drop database if exists catalogodb;
CREATE DATABASE IF NOT EXISTS catalogodb;
show tables from catalogodb;
use catalogodb;

INSERT INTO category (name) VALUES ('Ficção');

INSERT INTO category (name) VALUES ('Biografia');

INSERT INTO subcategory (name, category_id) VALUES ('Fantasia', 1);
INSERT INTO subcategory (name, category_id) VALUES ('Biografia Futebolística', 2);

INSERT INTO author (author_name) VALUES ('J.K. Rowling');
INSERT INTO author (author_name) VALUES ('Cristiano Ronaldo');

INSERT INTO book (
  title,
  isbn_number,
  image,
  description,
  price,
  quantity,
  author_id,
  category_id,
  subcategory_id
) VALUES (
  'Harry Potter e a Pedra Filosofal',
  '9789722325961',
  NULL,
  'Livro de fantasia e aventura.',
  14.99,
  10,
  1,
  1,
  1
);

INSERT INTO book (
  title,
  isbn_number,
  image,
  description,
  price,
  quantity,
  author_id,
  category_id,
  subcategory_id
) VALUES (
  'Harry Potter e a Câmara dos Segredos',
  '9789722327331',
  NULL,
  'Segundo volume da saga Harry Potter.',
  16.50,
  8,
  1,
  1,
  1
);

INSERT INTO book (
  title,
  isbn_number,
  image,
  description,
  price,
  quantity,
  author_id,
  category_id,
  subcategory_id
) VALUES (
  'A Vida de Cristiano Ronaldo',
  '9789722325941',
  NULL,
  'História de Vida.',
  24.99,
  1000,
  2,
  2,
  2
);

UPDATE book
SET quantity = 200
WHERE title = 'Harry Potter e a Pedra Filosofal';

select * from category;
select * from subcategory;
select * from author;
select * from book;


#===========================SERVIÇO DE CARRINHO==============================
DROP DATABASE IF EXISTS carrinhodb;
CREATE DATABASE carrinhodb;
USE carrinhodb;
show tables from carrinhodb;

select * from carrinhodb.cart;
select * from carrinhodb.cart_item;

insert into carrinhodb.cart (user_id, username) values ('1', 'MarcoHoracio');

delete from carrinhodb.cart where username = 'MarcoHoracio';

UPDATE carrinhodb.cart
   SET locked = FALSE
 WHERE user_id = 4;

UPDATE cart
   SET created_date = '2025-06-04'
 WHERE user_id = 1;

delete from carrinhodb.cart_item where username = "user_26179";


#===========================SERVIÇO DE SHIPPING==============================
DROP DATABASE IF EXISTS shippingdb;
CREATE DATABASE shippingdb;
USE shippingdb;
show tables from shippingdb;

#Inserir algo na shipping order:
#INSERT INTO shipping_order (first_name, last_name, address, city, email, postal_code)
#VALUES ('João', 'Silva', 'Rua dos Livros, 123', 'Lisboa', 'joao@email.com', '1000-001');

#Inserir em orders;
#INSERT INTO orders (order_date, total_price)
#VALUES (NOW(), 29.98);

#Inserir em order_details:
#INSERT INTO order_details (quantity, sub_total, book_id, shippingorder_id, user_id)
#VALUES (1, 14.99, 1, 7, 1);


select * from shippingdb.shipping_order;
select * from shippingdb.orders;
select * from shippingdb.order_details;




#===========================SERVIÇO DE QUERY==============================
drop database if exists querydb;
CREATE DATABASE IF NOT EXISTS querydb;
USE querydb;
show tables from querydb;

select * from querydb.query_order_items;
select * from querydb.query_orders;
select * from querydb.query_shipping;


INSERT INTO query_books (id, title, author, price) VALUES (
  1,
  'Harry Potter e a Pedra Filosofal',
  'J.K. Rowling',
  14.99
);



INSERT INTO query_books (id, title, author, price) VALUES (
  2,
  'Harry Potter e a Câmara dos Segredos',
  'J.K. Rowling',
  16.50
);



# ==========================COISAS ATOA ============================
USE catalogodb;

SELECT * FROM book;
SELECT * FROM category;
SELECT * FROM subcategory;


use querydb;
show tables from querydb;

ALTER TABLE orders ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
ALTER TABLE query_orders ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING';

UPDATE cart
SET user_id = 1,
    locked = FALSE
WHERE username = 'joaosilva';


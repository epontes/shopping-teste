# E-commerce API - Teste Técnico

## Visão Geral

Este projeto implementa uma API de e-commerce completa com gerenciamento de produtos, sistema de autenticação JWT, integração com Elasticsearch para buscas avançadas e processamento assíncrono via Kafka.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.x
- MySQL 8
- Elasticsearch 7.x
- Kafka
- Flyway para migração de banco de dados
- JWT para autenticação
- Arquitetura limpa

## Configuração do Ambiente

### Requisitos

- JDK 17+
- Docker e Docker Compose
- Maven

### Passos para Executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/shopping-teste.git
   cd shopping-teste
   ```

2. Inicie os serviços com Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. Execute a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

## Uso da API

### Autenticação

#### Criar um usuário:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario",
    "email": "usuario@exemplo.com",
    "password": "senha123",
    "fullName": "Usuário Teste"
  }'
```

#### Criar um usuário administrador:

```bash
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@exemplo.com",
    "password": "admin123",
    "fullName": "Administrador"
  }'
```

#### Login (obter token JWT):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario",
    "password": "senha123"
  }'
```

A resposta incluirá um token JWT que deve ser incluído em todas as requisições autenticadas:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "usuario",
  "email": "usuario@exemplo.com",
  "userRole": "USER"
}
```

### Produtos

#### Listar todos os produtos:

```bash
curl -X GET http://localhost:8080/api/products
```

#### Buscar produtos por nome (usando Elasticsearch):

```bash
curl -X GET "http://localhost:8080/api/products/search/name?name=smartphone"
```

#### Buscar produtos por categoria:

```bash
curl -X GET "http://localhost:8080/api/products/search/category?category=Eletrônicos"
```

#### Buscar produtos por faixa de preço:

```bash
curl -X GET "http://localhost:8080/api/products/search/price-range?minPrice=100&maxPrice=500"
```

#### Criar um produto (requer acesso de ADMIN):

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "name": "Novo Produto",
    "description": "Descrição do produto",
    "price": 99.90,
    "stock": 50,
    "category": "Eletrônicos",
    "active": true
  }'
```

### Pedidos

#### Criar um pedido:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 3,
        "quantity": 1
      }
    ]
  }'
```

A resposta incluirá os detalhes do pedido criado, incluindo o ID do pedido.

#### Realizar pagamento do pedido:

```bash
curl -X POST http://localhost:8080/api/orders/1/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "paymentMethod": "CREDIT_CARD",
    "amount": 299.90
  }'
```

Observação: O valor do pagamento deve corresponder exatamente ao valor total do pedido.

#### Listar pedidos do usuário atual:

```bash
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

#### Obter detalhes de um pedido específico:

```bash
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

## Fluxo Principal

1. Registre um usuário (ou use o usuário administrador)
2. Faça login para obter o token JWT
3. Navegue pelos produtos disponíveis
4. Crie um pedido com os produtos desejados
5. Realize o pagamento do pedido
6. O sistema:
   - Atualiza o status do pedido para PAGO
   - Publica um evento no Kafka
   - Atualiza automaticamente o estoque dos produtos
   - Atualiza os produtos no Elasticsearch

## Arquitetura

O projeto segue os princípios de arquitetura limpa com as seguintes camadas:

- **Presentation**: Controllers REST
- **Application**: DTOs e Services
- **Domain**: Entidades e Regras de negócio
- **Infrastructure**: Implementações técnicas (Kafka, Elasticsearch, etc.)

## Características Implementadas

- ✅ Autenticação segura com JWT
- ✅ Perfis de usuário (ADMIN e USER)
- ✅ CRUD completo de produtos
- ✅ Busca eficiente com Elasticsearch
- ✅ Sistema de pedidos
- ✅ Verificação de estoque
- ✅ Processamento assíncrono via Kafka
- ✅ Migração de banco de dados com Flyway

## Monitoramento

### Elasticsearch

O Kibana está disponível em `http://localhost:5601` para monitorar e visualizar os dados do Elasticsearch.

### Kafka

Para monitorar o Kafka, você pode acessar o contêiner:

```bash
docker exec -it ecommerce-kafka bash
```

E utilizar os comandos do Kafka para listar tópicos e mensagens:

```bash
kafka-topics --bootstrap-server localhost:9092 --list

kafka-console-consumer --bootstrap-server localhost:9092 --topic order.created --from-beginning
```

## Suporte e Problemas Conhecidos

- Se houver problemas de conexão com o Elasticsearch, verifique se o serviço está rodando:
  ```bash
  docker ps | grep elasticsearch
  ```

- Para reiniciar os serviços:
  ```bash
  docker-compose down
  docker-compose up -d
  ```

---

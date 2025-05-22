# Sistema Bancário - API REST

Sistema de gestão de notas fiscais e estoque desenvolvido com Spring Boot.

## Descrição do Projeto

A aplicação simula operações bancárias básicas, como:
- Criação de contas bancárias
- Transferência de valores entre contas
- Geração de relatórios de transações

Foi desenvolvida com foco em boas práticas como:
- Padrões REST
- Validações com Bean Validation (`javax.validation`)
- Arquitetura Hexagonal

  ### Backend
- Java Spring Boot
- MongoDB
- JPA/Hibernate
- Maven

## 🚀 Como Rodar a Aplicação

### Pré-requisitos

- Java 17+
- Maven ou Gradle
- MongoDB (instalado localmente ou hospedado remotamente)

### Executando com Maven

```bash
mvn clean install
mvn spring-boot:run

## Exemplos de Requisições

### Criar Conta Bancária

```bash
curl -X POST http://localhost:8080/accounts \
-H "Content-Type: application/json" \
-d '{
  "accountNumber": "123456",
  "ownerName": "Bruno",
  "initialBalance": 1000
}'

### Consultar saldo por ID da conta

curl -X GET http://localhost:8080/balance/1234567890

### Realizar Transferência

curl -X POST http://localhost:8080/transactions \
-H "Content-Type: application/json" \
-d '{
  "sourceAccount": "123456",
  "destinationAccount": "654321",
  "value": 250.00
}'

### Listar transações de uma conta com paginação

curl -X GET "http://localhost:8080/transactions/1234567890?page=0&size=10"

### Reverter uma transação pelo ID da transação

curl -X POST http://localhost:8080/reversed/transaction/abcdef1234567890

### Gerar Relatório de Transações

curl -X GET http://localhost:8080/reports/transactions?startDate=2025-01-01&endDate=2025-05-22


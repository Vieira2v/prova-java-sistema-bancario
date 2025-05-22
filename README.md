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
-d '  {
    "name": "Nome",
    "cpf": "123.456.789-10"
  }'

### Consultar saldo por ID da conta

curl -X 'GET' \
  'http://localhost:8080/v1/api/banking/system/balance/682f79e1301fe71481e78149' \
  -H 'accept: application/json'

### Realizar Transferência

curl -X 'POST' \
  'http://localhost:8080/v1/api/banking/system/transaction' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "sourceAccount": "937740",
  "destinationAccount": "529976",
  "value": 700
}'

### Listar transações de uma conta com paginação

curl -X GET "http://localhost:8080/transactions/1234567890?page=0&size=10"

### Reverter uma transação pelo ID da transação

curl -X POST http://localhost:8080/reversed/transaction/abcdef1234567890

### Gerar Relatório de Transações

curl -X GET http://localhost:8080/reports


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
- Spring Data MongoDB
- Maven

## 🚀 Como Rodar a Aplicação

### Pré-requisitos

- Java 17+
- Maven ou Gradle
- MongoDB (instalado localmente ou hospedado remotamente)

### Executando com Maven

```bash
cd sistema-bancario
mvn clean install
mvn spring-boot:run
```
### Documentação da API

http://localhost:8080/swagger-ui/index.html

## 🔐 Autenticação com JWT (Acesso Administrativo)

Para proteger os endpoints da API, foi implementada uma autenticação simples com JWT (JSON Web Token). Essa autenticação não representa clientes ou contas bancárias, mas sim um usuário administrador do sistema.
A autenticação serve apenas para controlar o acesso à API durante o uso (especialmente em ambiente local ou testes), garantindo que apenas usuários autenticados possam acessar operações sensíveis como transferências, saldos e relatórios.

## ✅ Cobertura de Testes

Para garantir a qualidade do código, foi utilizada a ferramenta JaCoCo para medir a cobertura de testes unitários.

Para gerar o relatório de cobertura, execute:

```bash
mvn clean test
mvn jacoco:report
```

O relatório será gerado em:

```bash
target/site/jacoco/index.html
```

## Exemplos de Requisições

### Criar Usuário (Apenas para acesso à API)

```bash
curl -X POST http://localhost:8080/v1/api/banking/register \
-H "Content-Type: application/json" \
-d '  {
    "username": "admin",
    "password": "123456"
  }'
```

### Login e Obtenção do Token

```bash
curl -X POST http://localhost:8080/v1/api/banking/login \
-H "Content-Type: application/json" \
-d '  {
    "username": "admin",
    "password": "123456"
  }'
```

### Usando o Token nos Endpoints Protegidos

Adicione o token no cabeçalho das requisições para acessar a API:

-H "Authorization: Bearer SEU_TOKEN_AQUI"

### Exemplo de Requisição com Token

```bash
curl -X POST http://localhost:8080/v1/api/banking/system/report \
-H "Authorization: Bearer SEU_TOKEN"
```

### Testando via Swagger

Acesse o Swagger em: http://localhost:8080/swagger-ui/index.html

Clique em "Authorize".

Cole o token no formato Bearer SEU_TOKEN.

Teste os endpoints protegidos diretamente pela interface.

### Refresh Token

```bash
curl -X POST http://localhost:8080/v1/api/banking/refresh/{username} \
-H "Content-Type: application/json" \
-H "Authorization: refresh-token"
```

### Criar Conta Bancária

```bash
curl -X POST http://localhost:8080/v1/api/banking/system/register \
-H "Content-Type: application/json" \
-H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
-d '  {
    "name": "Nome",
    "cpf": "123.456.789-10"
  }'
```

### Consultar saldo por ID da conta

```bash
curl -X 'GET' \
  'http://localhost:8080/v1/api/banking/system/balance/682f79e1301fe71481e78149' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9
```

### Realizar Transferência

```bash
curl -X 'POST' \
  'http://localhost:8080/v1/api/banking/system/transaction' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9
  -d '{
  "sourceAccount": "937740",
  "destinationAccount": "529976",
  "value": 700
}'
```

### Listar transações de uma conta com paginação

```bash
curl -X 'GET' \
    "http://localhost:8080/transactions/1234567890?page=0&size=10" \
    -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9
```

### Reverter uma transação pelo ID da transação

```bash
curl -X 'POST' \ 
  'http://localhost:8080/reversed/transaction/abcdef1234567890' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9
```

### Gerar Relatório de Transações

```bash
curl -X 'GET' \ 
  'http://localhost:8080/reports'
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9
```

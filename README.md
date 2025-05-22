# Sistema Bancário - API REST

Sistema de gestão de notas fiscais e estoque desenvolvido com React e Spring Boot.

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

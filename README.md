# Notification Service

Serviço de notificações com fila (RabbitMQ), persistência (Postgres), retry e envio de e‑mail via SMTP.

## Visão Geral
- **API REST** para criar e consultar notificações
- **Fila** para processamento assíncrono (RabbitMQ)
- **Persistência** em Postgres (JPA/Hibernate)
- **Retry automático** com backoff
- **Envio de e‑mail real** via SMTP

## Stack
- Java 21 + Spring Boot 4
- Postgres 16
- RabbitMQ 3
- Docker + Docker Compose

## Como Rodar

1. Configure o `.env`:
   - Copie `.env.example` para `.env` e preencha as variáveis.
2. Suba o ambiente:
```bash
make up
```

## Comandos Úteis (Makefile)
```bash
make up        # sobe tudo (build + up)
make down      # derruba os serviços
make ps        # status dos containers
make logs      # logs (SERVICE=notification-service)
make clean-db  # limpa tabela notifications
make reset     # apaga volumes (FULL RESET)
```

## Variáveis de Ambiente
Arquivo `.env`:
```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seu_email@gmail.com
SMTP_PASS=sua_senha_de_app
SMTP_FROM=seu_email@gmail.com

DB_URL=jdbc:postgresql://postgres:5432/notification_db
DB_NAME=notification_db
DB_USER=notification
DB_PASS=notification

RABBIT_HOST=rabbitmq
RABBIT_PORT=5672
RABBIT_USER=guest
RABBIT_PASS=guest
```

> **Gmail:** use **Senha de app** no `SMTP_PASS` (não a senha normal).

## Endpoints

### Criar notificação
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 123e4567" \
  -d '{
    "channel":"EMAIL",
    "recipient":"seu_email@gmail.com",
    "title":"Teste",
    "message":"Primeira notificação"
  }'
```

> A responsabilidade de gerar e enviar o `Idempotency-Key` é do cliente.  
> Se repetir o mesmo `Idempotency-Key`, a API retorna a mesma notificação.

Resposta:
```json
{
  "id": "uuid",
  "status": "PENDING"
}
```

### Erro de validação (400)
```json
{
  "error": "VALIDATION_ERROR",
  "fields": {
    "recipient": "must be a well-formed email address",
    "title": "must not be blank"
  }
}
```

### Erro de recurso não encontrado (404)
```json
{
  "error": "Notification not found",
  "fields": {}
}
```

### Consultar status
```bash
curl http://localhost:8080/api/v1/notifications/{UUID}
```

### Dashboard
```bash
curl http://localhost:8080/api/v1/notifications/dashboard
```
Resposta inclui `pendingTotal`, que soma `PENDING + PROCESSING + RETRYING`.

### Listar todas
```bash
curl http://localhost:8080/api/v1/notifications
```

## Healthcheck
Actuator habilitado:
```bash
curl http://localhost:8080/actuator/health
```

Se quiser liveness/readiness:
```bash
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

## Como Funciona o Fluxo
1. `POST /notifications` cria e salva no banco
2. ID é publicado na fila do RabbitMQ
3. Worker consome a fila, processa e envia
4. Status muda para `SENT`, `RETRYING` ou `FAILED`

### Recuperação de `PROCESSING`
Se uma notificação ficar presa em `PROCESSING` por muito tempo (ex.: queda do serviço),
um scheduler marca como `RETRYING` para que ela volte a ser processada.  
Configurações:
```
notification.processing.timeout-seconds=120
notification.processing.recovery-delay-ms=10000
```

## Observações
- `SENT` significa que o SMTP aceitou o envio.  
  Se o destinatário não existir, o Gmail envia um bounce depois.

## Roadmap (sugestões para portfólio)
- Tratamento de validação com erros em JSON
- Healthcheck/metrics (Spring Actuator)
- Testes de integração com Postgres/RabbitMQ
- Recuperação de notificações travadas em `PROCESSING`

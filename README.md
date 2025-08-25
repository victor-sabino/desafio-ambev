
# Order Service (Spring Boot + MongoDB + WebFlux + Resilience4j)

> Serviço **order** que recebe/gera pedidos, calcula o total, persiste no MongoDB e disponibiliza os pedidos para consulta. Após o processamento, o serviço exporta o pedido para o **Produto Externo B**. Implementa resiliência (Circuit Breaker, Retry, Rate Limiter, TimeLimiter, Outbox + Retry) para suportar **1000 rpm** (~16.7 rps).

## 🧭 Objetivo
- Integrar com **Produto Externo A** para receber pedidos.
- Realizar a **gestão e cálculo do valor total** dos produtos por pedido.
- Disponibilizar **consulta de pedidos** e/ou produtos.
- Enviar pedidos calculados para **Produto Externo B**.
- Considerar **verificação de duplicidade** e **alta volumetria**.
- Implementar **apenas** o serviço `order` (A/B são simulados).


## 📦 Endpoints
- `POST /api/orders` — cria/atualiza pedido (idempotente por `externalOrderId`) e agenda exportação p/ B.
- `GET  /api/orders` — lista pedidos.
- `GET  /api/orders/{id}` — consulta pedido por id.
- `POST /api/orders/integrations/a/pull` — busca pedidos pendentes no **A** e processa.

## 🔌 Como simular os Produtos Externos A e B (WireMock)



### Subir os simuladores
```bash
docker compose up -d mongo wiremock-a wiremock-b
```

### 1) Conferir o **Produto Externo A**
O A possui um stub `GET /orders/pending` que devolve um pedido pendente.
```bash
curl http://localhost:8081/orders/pending | jq
```
Você pode editar `wiremock/a/mappings/a-orders.json` para ajustar os pedidos retornados e reiniciar o container:
```bash
docker compose restart wiremock-a
```

### 2) Processar pedidos vindos do A
O serviço `order` busca no A e processa:
```bash
curl -X POST http://localhost:8080/api/orders/integrations/a/pull | jq
```
Isso criará/atualizará os pedidos, calculará o **total** e tentará exportá-los para o B.

### 3) Conferir o **Produto Externo B**
O B aceita `POST /orders/processed` (retorna 202).
Para enviar manualmente um pedido, crie um e observe o envio ao B:
```bash
curl -X POST http://localhost:8080/api/orders  -H "Content-Type: application/json"  -d '{"externalOrderId":"A-999","customerId":"C-1","items":[{"productId":"P1","name":"X","price":10,"quantity":1}]}' | jq
```

### 4) Simular **falha do B** (testar outbox + retry)
**Opção A — parar o B:**
```bash
docker compose stop wiremock-b
# faça uma chamada ao POST /api/orders para gerar um pedido
# um registro será criado em "outbox_export"; o job tentará reenviar periodicamente
docker compose start wiremock-b
```

**Opção B — forçar 500 no B via API de admin do WireMock:**
```bash
# coloca um stub com maior prioridade (1) retornando 500 para /orders/processed
curl -X POST http://localhost:8082/__admin/mappings  -H "Content-Type: application/json"  -d '{ "priority": 1, "request": { "method": "POST", "url": "/orders/processed" }, "response": { "status": 500 } }'

# desfaz (reset) e volta a aceitar 202
curl -X POST http://localhost:8082/__admin/mappings/reset
```
Enquanto o B estiver indisponível, a exportação falhará e o serviço registrará o evento na **outbox** para retries.

### 5) Inspecionar a **outbox** e os pedidos no Mongo
```bash
# requer mongosh instalado localmente
mongosh "mongodb://root:root@localhost:27017/orderdb?authSource=admin" --eval 'db.outbox_export.find().pretty()'
mongosh "mongodb://root:root@localhost:27017/orderdb?authSource=admin" --eval 'db.orders.find().pretty()'
```

## ▶️ Como rodar
1) Suba o Mongo e os simuladores (A/B):
```bash
docker compose up -d
```

2) Rode a aplicação:
```bash
mvn spring-boot:run
```

## ⚙️ Performance & Resiliência
- **CB / Retry / TimeLimiter / RateLimiter** nas integrações.
- **Outbox + Retry Job** garante entrega ao B.
- **Idempotência** por `externalOrderId` (índice único).
- **WebFlux** e **threads virtuais** habilitadas.

## 🧪 Observabilidade
- `GET /actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.


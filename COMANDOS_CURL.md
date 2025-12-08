# Comandos cURL - Pix Service

Base URL: `http://localhost:8080` (ou a que voce preferir)

## 📦 Carteiras

### 1. Criar Carteira
```bash
curl -X POST http://localhost:8080/carteiras/user-123 \
  -H "Content-Type: application/json"
```

**Resposta esperada:** Cria uma carteira para o proprietário `user-123` e retorna os dados da carteira (incluindo UUID).

---

### 2. Depositar na Carteira
```bash
curl -X POST http://localhost:8080/carteiras/{carteiraId}/depositar \
  -H "Content-Type: application/json" \
  -d '{
    "quantidade": 100.00
  }'
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/carteiras/550e8400-e29b-41d4-a716-446655440000/depositar \
  -H "Content-Type: application/json" \
  -d '{
    "quantidade": 100.00
  }'
```

---

### 3. Sacar da Carteira
```bash
curl -X POST http://localhost:8080/carteiras/{carteiraId}/sacar \
  -H "Content-Type: application/json" \
  -d '{
    "quantidade": 50.00
  }'
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/carteiras/550e8400-e29b-41d4-a716-446655440000/sacar \
  -H "Content-Type: application/json" \
  -d '{
    "quantidade": 50.00
  }'
```

---

### 4. Consultar Saldo (Atual)
```bash
curl -X GET http://localhost:8080/carteiras/{carteiraId}/saldo \
  -H "Content-Type: application/json"
```

**Exemplo:**
```bash
curl -X GET http://localhost:8080/carteiras/550e8400-e29b-41d4-a716-446655440000/saldo \
  -H "Content-Type: application/json"
```

---

### 5. Consultar Saldo Histórico
```bash
curl -X GET "http://localhost:8080/carteiras/{carteiraId}/saldo?data=2025-01-15T10:30:00" \
  -H "Content-Type: application/json"
```

**Exemplo:**
```bash
curl -X GET "http://localhost:8080/carteiras/550e8400-e29b-41d4-a716-446655440000/saldo?data=2025-01-15T10:30:00" \
  -H "Content-Type: application/json"
```

**Nota:** O formato da data é `YYYY-MM-DDTHH:mm:ss` (ISO LocalDateTime).

---

## 🔑 Chaves PIX

### 6. Registrar Chave PIX
```bash
curl -X POST http://localhost:8080/carteiras/{carteiraId}/chaves-pix \
  -H "Content-Type: application/json" \
  -d '{
    "tipoChavePix": "EMAIL",
    "valorChave": "usuario@example.com"
  }'
```

**Tipos de Chave PIX disponíveis:**
- `EMAIL` - Email do usuário
- `PHONE` - Telefone (formato: +5511999999999)
- `EVP` - Chave aleatória (UUID)

**Exemplos:**

**Email:**
```bash
curl -X POST http://localhost:8080/carteiras/550e8400-e29b-41d4-a716-446655440000/chaves-pix \
  -H "Content-Type: application/json" \
  -d '{
    "tipoChavePix": "EMAIL",
    "valorChave": "usuario@example.com"
  }'
```

**Telefone:**
```bash
curl -X POST http://localhost:8080/carteiras/550e8400-e29b-41d4-a716-446655440000/chaves-pix \
  -H "Content-Type: application/json" \
  -d '{
    "tipoChavePix": "PHONE",
    "valorChave": "+5511999999999"
  }'
```

**EVP (Chave Aleatória):**
```bash
curl -X POST http://localhost:8080/carteiras/550e8400-e29b-41d4-a716-446655440000/chaves-pix \
  -H "Content-Type: application/json" \
  -d '{
    "tipoChavePix": "EVP",
    "valorChave": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

## 💸 Transferências PIX

### 7. Realizar Transferência PIX
```bash
curl -X POST http://localhost:8080/pix/transferencias \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: {uuid-idempotencia}" \
  -d '{
    "carteiraIdOrigem": "550e8400-e29b-41d4-a716-446655440000",
    "chavePixDestino": "usuario@example.com",
    "valor": 50.00
  }'
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/pix/transferencias \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{
    "carteiraIdOrigem": "550e8400-e29b-41d4-a716-446655440000",
    "chavePixDestino": "usuario@example.com",
    "valor": 50.00
  }'
```

**Importante:** 
- O header `Idempotency-Key` é obrigatório e garante que requisições duplicadas retornem o mesmo resultado.
- A transferência será criada com status `PENDING` e só será confirmada após o processamento do webhook.

---

## 🔔 Webhooks PIX

### 8. Processar Webhook (Transferência Confirmada)
```bash
curl -X POST http://localhost:8080/pix/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "referencia": "end-to-end-id-da-transferencia",
    "eventoId": "evento-unico-123",
    "tipoEvento": "CONFIRMADO",
    "dataDeOcorrencia": "2025-01-15T10:30:00"
  }'
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/pix/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "referencia": "E12345678202501151030123456789",
    "eventoId": "evt-123e4567-e89b-12d3-a456-426614174000",
    "tipoEvento": "CONFIRMADO",
    "dataDeOcorrencia": "2025-01-15T10:30:00"
  }'
```

---

### 9. Processar Webhook (Transferência Rejeitada)
```bash
curl -X POST http://localhost:8080/pix/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "referencia": "end-to-end-id-da-transferencia",
    "eventoId": "evento-unico-456",
    "tipoEvento": "REJEITADO",
    "dataDeOcorrencia": "2025-01-15T10:30:00"
  }'
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/pix/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "referencia": "E12345678202501151030123456789",
    "eventoId": "evt-456e7890-e89b-12d3-a456-426614174001",
    "tipoEvento": "REJEITADO",
    "dataDeOcorrencia": "2025-01-15T10:30:00"
  }'
```

**Tipos de Evento disponíveis:**
- `CONFIRMADO` - Transferência confirmada (débito/crédito aplicado)
- `REJEITADO` - Transferência rejeitada (sem alteração de saldo)

**Importante:**
- O `eventoId` deve ser único. Requisições com o mesmo `eventoId` são idempotentes.
- O `referencia` deve corresponder ao `endToEndId` retornado na criação da transferência.

---

## 📋 Fluxo Completo de Exemplo

### Passo 1: Criar duas carteiras
```bash
# Carteira 1
curl -X POST http://localhost:8080/carteiras/user-001

# Carteira 2
curl -X POST http://localhost:8080/carteiras/user-002
```

**Salve os UUIDs retornados!**

### Passo 2: Depositar na carteira de origem
```bash
curl -X POST http://localhost:8080/carteiras/{carteiraIdOrigem}/depositar \
  -H "Content-Type: application/json" \
  -d '{"quantidade": 1000.00}'
```

### Passo 3: Registrar chave PIX na carteira de destino
```bash
curl -X POST http://localhost:8080/carteiras/{carteiraIdDestino}/chaves-pix \
  -H "Content-Type: application/json" \
  -d '{
    "tipoChavePix": "EMAIL",
    "valorChave": "destino@example.com"
  }'
```

### Passo 4: Realizar transferência PIX
```bash
curl -X POST http://localhost:8080/pix/transferencias \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{
    "carteiraIdOrigem": "{carteiraIdOrigem}",
    "chavePixDestino": "destino@example.com",
    "valor": 250.00
  }'
```

**Salve o `referencia` (endToEndId) retornado!**

### Passo 5: Processar webhook de confirmação
```bash
curl -X POST http://localhost:8080/pix/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "referencia": "{referencia-do-passo-4}",
    "eventoId": "$(uuidgen)",
    "tipoEvento": "CONFIRMADO",
    "dataDeOcorrencia": "2025-01-15T10:30:00"
  }'
```

### Passo 6: Verificar saldos
```bash
# Saldo carteira origem
curl -X GET http://localhost:8080/carteiras/{carteiraIdOrigem}/saldo

# Saldo carteira destino
curl -X GET http://localhost:8080/carteiras/{carteiraIdDestino}/saldo
```

---

## 🔧 Observações Importantes

1. **Idempotência**: 
   - Transferências PIX: use `Idempotency-Key` único por requisição
   - Webhooks: use `eventoId` único por evento

2. **Formato de Data**: 
   - Use formato ISO LocalDateTime: `YYYY-MM-DDTHH:mm:ss`
   - Exemplo: `2025-01-15T10:30:00`

3. **Valores Monetários**: 
   - Use formato decimal com ponto: `100.00`
   - Valor mínimo: `0.01`

4. **UUIDs**: 
   - Use formato UUID padrão: `550e8400-e29b-41d4-a716-446655440000`
   - No Windows PowerShell, use: `[guid]::NewGuid().ToString()`
   - No Linux/Mac, use: `uuidgen` ou gere online

---

## 📊 Health Check (Actuator)

### 10. Health Check
```bash
curl -X GET http://localhost:8080/actuator/health
```

### 11. Métricas
```bash
curl -X GET http://localhost:8080/actuator/metrics
```

### 12. Info
```bash
curl -X GET http://localhost:8080/actuator/info
```


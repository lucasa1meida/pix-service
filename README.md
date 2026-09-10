## Pix Service - Code Assessment

Microserviço de carteira com suporte a Pix, com foco em consistência sob concorrência, idempotência e auditabilidade. Esta documentação resume o que o projeto faz, como está estruturado e como executá‑lo / testá‑lo.

---

### Stack & Arquitetura

- **Linguagem**: Java 17  
- **Framework**: Spring Boot 3.3.x  
- **Persistência**:
  - **Produção**: PostgreSQL (`org.postgresql:postgresql`)
  - **Testes**: Usando testContainers
- **Outras libs**:
  - **Spring Data JPA**
  - **Bean Validation** (Jakarta Validation)
  - **ModelMapper** (mapeamento entidade ↔ DTO)
  - **Micrometer + Actuator** (métricas)
  - **Lombok** (redução de boilerplate)
- **Arquitetura em camadas (Clean-ish)**:
  - **API (`controller`, `dto`, `excecoes`)**
    - `com.pixservice.carteira.controller.CarteiraController`
    - `com.pixservice.pix.controller.PixController`
    - DTOs de entrada/saída e `GlobalExceptionHandler`.
  - **Aplicação/Serviços**
    - `com.pixservice.carteira.service.CarteiraService`
    - `com.pixservice.pix.service.PixService`
    - `com.pixservice.pix.service.TransferenciaPixService`
  - **Domínio (Entidades + Regras)**
    - `Carteira`, `ChavePix`, `TransferenciaPix`, `HistoricoTransacao`,
      `RegistroIdempotencia`, `EventoPixWebhook`
    - Enums: `TipoChavePix`, `StatusTransferenciaPix`, `TipoEventoPixWebhook`,
      `TipoTransacao`, `NaturezaTransacao`
    - Exceções de domínio: `ExcecaoDeDominio`
  - **Infraestrutura (Repositórios JPA, Configuração)**
    - Repositórios Spring Data: `CarteiraRepository`, `ChavePixRepository`,
      `TransferenciaPixRepository`, `HistoricoTransacaoRepository`,
      `RegistroIdempotenciaRepository`, `EventoWebhookRepository`
    - Configuração de `ModelMapper` (`MapperConfig`)
    - Uso de locking pessimista via `@Lock(PESSIMISTIC_WRITE)`.

---

### Como rodar

**Pré‑requisitos**

- Java 17+
- Maven 3.9+
- Docker + Docker Compose

#### 1. Banco de dados (Postgres via Docker Compose)

O projeto já inclui um `docker-compose.yml` com um serviço Postgres configurado com:

- **DB**: `pixservice`
- **User**: `pix`
- **Senha**: `pix`

Para subir o banco:

```bash
docker-compose up -d
```

Depois disso, a aplicação consegue se conectar usando a URL padrão do `application.properties` principal (apontando para `localhost:5432` com essas credenciais). Ajuste a `spring.datasource.url` / `username` / `password` se necessário.

#### 2. Rodar a aplicação

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

#### 3. Rodar os testes

```bash
mvn test
```

- Em **testes**, é usado **H2 em memória** com schema criado via scripts SQL em `src/test/resources/sql`.
- O schema e os datasets são carregados com `@Sql` nos testes de integração.

---

### Endpoints principais

OBS: temos um md(COMANDOS_CURL.md) com exemplos de curl para facilitar os testes.

#### Carteira (`CarteiraController` – base `/carteiras`)

- **Criar carteira**

  - **POST** `/carteiras/{proprietarioId}`
  - **Path param**: `proprietarioId` (string)
  - **Resposta (201)** – `CarteiraDTO`:

    ```json
    {
      "carteiraId": "uuid",
      "propietarioId": "user-123",
      "saldo": 0.00,
      "dataDeCriacao": "2025-01-01T10:00:00",
      "dataDeAtualizacao": "2025-01-01T10:00:00"
    }
    ```

- **Depósito**

  - **POST** `/carteiras/{carteiraId}/depositar`
  - **Path param**: `carteiraId` (UUID)
  - **Body** – `MovimentacaoCarteiraDTO`:

    ```json
    { "quantidade": 100.00 }
    ```

  - **Resposta (200)** – `CarteiraDTO` atualizado.

- **Saque**

  - **POST** `/carteiras/{carteiraId}/sacar`
  - **Path param**: `carteiraId` (UUID)
  - **Body** – `MovimentacaoCarteiraDTO`:

    ```json
    { "quantidade": 50.00 }
    ```

  - **Valida saldo disponível** (caso contrário lança `ExcecaoDeDominio`).
  - **Resposta (200)** – `CarteiraDTO` com saldo atualizado.

- **Saldo atual / histórico**

  - **GET** `/carteiras/{carteiraId}/saldo`
  - **Query param opcional**: `data` (ex.: `2025-10-09T15:00:00`)
  - **Sem `data`**:
    - Busca a entidade `Carteira` e retorna saldo atual.
  - **Com `data`**:
    - Usa `HistoricoTransacaoRepository.findFirstByDataTransacaoIsLessThanEqualAndCarteiraIdOrderByDataTransacaoDesc`
      para recuperar o último valor conhecido até aquele timestamp.
  - **Resposta (200)** – `SaldoDTO`:

    ```json
    {
      "saldo": 150.00,
      "data": "2025-10-09T15:00:00"
    }
    ```

---

#### Pix (`PixController`)

- **Registrar chave Pix**

  - **POST** `/carteiras/{carteiraId}/chaves-pix`
  - **Path param**: `carteiraId` (UUID)
  - **Body** – `RegistroChavePixDTO`:

    ```json
    {
      "tipoChavePix": "EMAIL",
      "valorChave": "user@example.com"
    }
    ```

  - Verifica existência da carteira; cria `ChavePix` com constraint única em `valor_chave`.
  - **Resposta (201)** – `ChavePixDTO`:

    ```json
    {
      "id": "uuid",
      "carteiraId": "uuid",
      "tipoChavePix": "EMAIL",
      "valorChave": "user@example.com",
      "ativo": true,
      "dataDeCriacao": "...",
      "dataDeAtualizacao": "..."
    }
    ```

- **Transferência Pix interna (inicia fluxo)**

  - **POST** `/pix/transferencias`
  - **Header obrigatório**: `Idempotency-Key: <string>`
  - **Body** – `RequisicaoPixTransferenciaDTO`:

    ```json
    {
      "carteiraIdOrigem": "uuid",
      "chavePixDestino": "destino@example.com",
      "valor": 100.00
    }
    ```

  - **Resposta (200)** – `TransferenciaPixDTO`:

    ```json
    {
      "id": "uuid",
      "referencia": "E12345678202501151030123456789",
      "carteiraIdOrigem": "uuid-origem-ou-destino-dependendo-do-fluxo",
      "carteiraIdDestino": "uuid",
      "chavePix": "destino@example.com",
      "valor": 100.00,
      "dataDeCriacao": "...",
      "dataDeAtualizacao": "...",
      "status": "PENDENTE"
    }
    ```

- **Webhook Pix (simulado)**

  - **POST** `/pix/webhooks`
  - **Body** – `RequisicaoPixWebhookDTO`:

    ```json
    {
      "referencia": "E12345678202501151030123456789",
      "eventoId": "evt-123e4567-e89b-12d3-a456-426614174000",
      "tipoEvento": "CONFIRMADO",  // ou "REJEITADO"
      "dataDeOcorrencia": "2025-10-09T15:00:00"
    }
    ```

  - **Idempotente por `eventoId`**:
    - Se o evento já foi processado, apenas retorna o estado atual da transferência.
  - **Resposta (200)** – `TransferenciaPixDTO` com `status` atualizado (`CONFIRMADO` / `REJEITADO`).

---

### Idempotência & Concorrência

#### Idempotência de transferências Pix (`Idempotency-Key`)

- Entidade: `RegistroIdempotencia`
  - Campos: `id`, `chaveIdempotencia`, `referencia`, `dataDeCriacao`, `payloadHash`
  - Constraint única: `uk_chave_idempotencia` em `chave_idempotencia`
- Serviço: `TransferenciaPixService`

Fluxo em `PixService.transferir`:

1. Calcula um `payloadHash` canônico a partir de:
   - `carteiraIdOrigem`
   - `chavePixDestino`
   - `valor` (normalizado com `setScale(2, HALF_UP)`)
2. Chama `transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash)`:
   - Se **não há registro** em `RegistroIdempotencia` → retorna `null`.
   - Se há `RegistroIdempotencia` com **payloadHash diferente** → lança `ExcecaoDeDominio`:
     - `"A chave de idempotência já está sendo usado por outro paylod."`  
       (protege contra reuso incorreto de chave de idempotência).
   - Se há registro, mas **referência vazia/nula** → considera ainda não finalizado, retorna `null`.
   - Se há referência → busca `TransferenciaPix` por `referencia` e retorna `TransferenciaPixDTO` existente.
3. Se não houver transferência existente, chama:
   - `transferenciaPixService.obterTransferenciaPixNova(...)`:
     - Valida carteiras origem/destino e chave Pix destino.
     - Gera `endToEndId` (referência).
     - Cria `TransferenciaPix PENDENTE`.
     - Salva `RegistroIdempotencia(chaveIdempotencia, endToEndId, payloadHash)`.

**Efeito**: várias chamadas com o mesmo `Idempotency-Key` + mesmo payload retornam a **mesma transferência** (exactly-once).

#### Concorrência e saldo de carteira

- Repositório: `CarteiraRepository`
  - Método `findWithLockingById(UUID)` anotado com `@Lock(LockModeType.PESSIMISTIC_WRITE)`:
    - Tradução: `SELECT ... FOR UPDATE`, garantindo serialização de atualizações de saldo.
- Usado em:
  - `TransferenciaPixService.obterTransferenciaPixNova` (ao buscar carteiras envolvidas)
  - `PixService.processarWebhook` (confirmações Pix, débitos/créditos)
- Todas as operações críticas de Pix são **`@Transactional`**, garantindo:
  - Escrita do histórico (`HistoricoTransacao`)
  - Atualização de saldo de carteira
  - Atualização de estado da transferência (`TransferenciaPix.status`)
  - Registro de evento de webhook (`EventoPixWebhook`)
  - Tudo no mesmo boundary transacional.

#### Webhook duplicado e fora de ordem

- Entidade: `EventoPixWebhook`
  - Constraint única: `uk_pix_webhook_event_id` em `evento_id`.
- Serviço: `PixService.processarWebhook`:

  1. **Webhook duplicado**:
     - Tenta `eventoWebhookRepository.findByEventoId(request.getEventoId())`:
       - Se já existe:
         - Loga que o evento já foi processado.
         - Busca `TransferenciaPix` por `referencia` e retorna o estado atual (`TransferenciaPixDTO`).
         - **Nenhuma operação de débito/crédito é refeita.**

  2. **Novo evento**:
     - Persiste `EventoPixWebhook` (garantindo idempotência futura).
     - Busca `TransferenciaPix` por `referencia` (se não encontrar, lança `ExcecaoDeDominio`).
     - Aplica regra:
       - Se `tipoEvento == CONFIRMADO`:
         - Tenta `transferenciaPix.confirmar()`:
           - Máquina de estados em `TransferenciaPix` impede confirmações inválidas (ex.: de `REJEITADO` para `CONFIRMADO`).
         - Cria dois `HistoricoTransacao`:
           - `PIX_OUT` (débito carteira origem)
           - `PIX_IN` (crédito carteira destino)
         - Puxa carteiras com locking pessimista e aplica:
           - `carteiraOrigem.sacar(valor)`
           - `carteiraDestino.depositar(valor)`
       - Se `tipoEvento == REJEITADO`:
         - Chama `transferenciaPix.rejeitar()`.
     - Qualquer `ExcecaoDeDominio` durante essa etapa faz o serviço **retornar o estado original** da transferência, sem side effects adicionais.

**Efeitos nos cenários-chave:**

1. **Duplo disparo** (mesma `Idempotency-Key`):
   - `RegistroIdempotencia` garante que apenas uma transferência Pix é criada.
2. **Webhook duplicado**:
   - `EventoPixWebhook` com constraint em `evento_id` + checagem de existência evita reprocesso.
3. **Ordem trocada (REJECTED antes de CONFIRMED, etc.)**:
   - A máquina de estados de `TransferenciaPix` e o `try/catch` em `processarWebhook` garantem que transições inválidas não mudem o saldo final.
4. **Reprocesso (“at least once”)**:
   - Eventos repetidos retornam o estado atual sem refazer débitos/créditos.

---

### Ledger & Saldo histórico

- Entidade: `HistoricoTransacao`
  - Campos principais:
    - `carteiraId`
    - `valor` (saldo após a transação)
    - `tipoTransacao` (`DEPOSITO`, `SAQUE`, `PIX_OUT`, `PIX_IN`, `AJUSTE`)
    - `naturezaTransacao` (`CREDITO` / `DEBITO`)
    - `endToEndId` (para vincular a transferências Pix)
    - `dataTransacao`
  - Fábricas estáticas:
    - `depositar(carteiraId, valor, tipoTransacao, endToEndId)`
    - `sacar(carteiraId, valor, tipoTransacao, endToEndId)`

- **Depósito/Saque**:
  - `CarteiraService.depositar` / `sacar`:
    - Atualizam o saldo da carteira.
    - Persistem um `HistoricoTransacao` com o **saldo após** a operação.

- **Pix confirmado**:
  - `PixService.processarWebhook` cria dois lançamentos (`PIX_OUT` e `PIX_IN`) com o valor transferido e referência (`endToEndId`).

- **Saldo histórico** (`CarteiraService.obterSaldo`):

  - Quando `data != null`:

    ```java
    HistoricoTransacao h = historicoTransacaoRepository
        .findFirstByDataTransacaoIsLessThanEqualAndCarteiraIdOrderByDataTransacaoDesc(data, carteiraId);
    ```

  - Se não encontrar, lança `NotFoundException("Transação não encontrada.")`.
  - Caso encontre, retorna `SaldoDTO(h.getValor(), data)`.

---

### Observabilidade & Tratamento de erros

- **Logs**:
  - `CarteiraService` e `PixService` usam anotações de logger (`@Log4j2`, `@Slf4j`) e registram as principais operações (criação de carteira, transferências, processamento de webhook).
  - `GlobalExceptionHandler` loga erros com mensagens padronizadas.

- **Métricas (Micrometer)**:
  - `PixService` incrementa um contador `transferencias_pix_iniciadas` via `MeterRegistry` a cada nova transferência Pix iniciada.

- **Tratamento global de exceções** (`GlobalExceptionHandler`):

  - **`ExcecaoDeDominio`** → `400 BAD_REQUEST`
    - Corpo: `{ "message": "<mensagem>" }`
  - **`NotFoundException`** → `404 NOT_FOUND`
    - Corpo: `{ "message": "Dados não encontrados: ..." }`
  - **`InternalServerErrorException`** → `500 INTERNAL_SERVER_ERROR`
    - Corpo: `{ "message": "..." }`

Os controllers (`CarteiraController` e `PixController`) convertem erros de serviço em exceções específicas com prefixos (`"Erro ao salvar carteira: ..."`, etc.), garantindo mensagens claras para o consumidor.

---

### Testes

- **Unitários de domínio**:
  - `CarteiraTest`: valida construção, depósitos, saques e cenários de erro (valores nulos, negativos, saldo insuficiente).
  - `HistoricoTransacaoTest`: garante criação correta de lançamentos de crédito/débito e validações.
  - `ChavePixTest`, `RegistroIdempotenciaTest`, `TransferenciaPixTest`, `EventoPixWebhookTest`:
    - Cobrem regras de validação de construtores, estado inicial (`PENDENTE`), transições de estado (`confirmar/rejeitar`) e mensagens de erro de domínio.

- **Unitários de serviços**:
  - `CarteiraServiceTest`: cobre fluxos de criar carteira, obter saldo atual/histórico, depósitos/saques, exceções e integração com `HistoricoTransacao`.
  - `TransferenciaPixServiceTest`: cobre algoritmos de idempotência, criação de nova transferência Pix, validação de chaves Pix/carteiras envolvidas.
  - `PixServiceTest`: cobre fluxo de transferência Pix (existente/nova), processamento de webhook para `CONFIRMADO` e `REJEITADO`, e cenários de erro (transferência não encontrada, carteiras ausentes, etc.).

- **Integração (REST + H2 + SQL + JPA)**:
  - `CarteiraControllerIntegrationTest`:
    - Usa `@SpringBootTest`, `@AutoConfigureMockMvc` e `@Sql` com:
      - `sql/setup.sql` (schema)
      - `sql/datasets/carteira/carteira-controller-integration-test.sql` (dados seed)
    - Cobre:
      - Criação de carteira (201 + payload completo).
      - Depósito / saque com verificação detalhada do `CarteiraDTO` retornado.
      - Saldo atual e histórico.
      - Cenários de erro (proprietário vazio, valor inválido, saldo insuficiente, histórico ausente).

  - `CarteiraControllerErrorIntegrationTest`:
    - Usa `@MockBean CarteiraService` para forçar exceções e cobrir todos os `catch` de `CarteiraController` (`ExcecaoDeDominio` e `Exception`).

  - `PixControllerIntegrationTest`:
    - Verifica:
      - Registro de chave Pix.
      - Transferência Pix com `Idempotency-Key`.
      - Processamento de webhook `CONFIRMADO` (muda status para `CONFIRMADO`).
      - Cenários de erro (carteira de origem inexistente, transferência não encontrada no webhook).

  - `PixControllerErrorIntegrationTest`:
    - Usa `@MockBean PixService` para forçar erro genérico em `transferir` e cobrir o `catch (Exception)`.

---

### Assunções & Trade-offs

- **Escopo**:
  - Apenas Pix **interno** entre carteiras gerenciadas por este serviço.
  - Apenas três tipos de chave Pix: `EMAIL`, `PHONE`, `EVP`.
  - Moeda única (R$), sem suporte a multi‑moeda.

- **Autenticação/Autorização**:
  - Não implementada (focamos em regras de domínio, concorrência, idempotência e auditabilidade).

- **Lock pessimista**:
  - Escolhido para simplificar raciocínio sobre concorrência em saldo (trade‑off: menos throughput sob alta contenção).

- **Idempotência focada em Pix**:
  - Implementada exclusivamente para transferências Pix (usando `RegistroIdempotencia`) e eventos de webhook (via `EventoPixWebhook`).
  - Modelo é facilmente estendível a outros fluxos.

- **Tempo**:
  - Alguns aspectos (por exemplo, mais métricas, logs estruturados mais ricos, testes de carga/concorrência) podem ser aprofundados com mais tempo.

---

### Tempo investido


- **Modelagem e arquitetura**: ~3h  
- **Implementação de domínio e serviços**: ~10h  
- **API REST, observabilidade, tratamento de erros**: ~10h  
- **Testes (unitários + integração) e refinamentos**: ~3h  

**Total aproximado**: 26h.



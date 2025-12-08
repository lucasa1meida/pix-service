CREATE SCHEMA IF NOT EXISTS public;

-- Tabela de carteiras
CREATE TABLE IF NOT EXISTS public.carteira (
    id UUID PRIMARY KEY,
    ordem_id VARCHAR(255) NOT NULL,
    saldo NUMERIC(19, 2) NOT NULL,
    data_de_criacao TIMESTAMP NOT NULL,
    data_de_atualizacao TIMESTAMP NOT NULL
);

-- Histórico de transações
CREATE TABLE IF NOT EXISTS public.historico_transacao (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    tipo_transacao VARCHAR(50) NOT NULL,
    natureza_transacao VARCHAR(50) NOT NULL,
    end_to_end_id VARCHAR(255),
    data_transacao TIMESTAMP NOT NULL
);

-- Chaves Pix
CREATE TABLE IF NOT EXISTS public.chave_pix (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    tipo_chave_pix VARCHAR(50) NOT NULL,
    valor_chave VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL,
    data_de_criacao TIMESTAMP NOT NULL,
    data_de_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT uk_valor_chave UNIQUE (valor_chave)
);

-- Transferências Pix
CREATE TABLE IF NOT EXISTS public.transferencia_pix (
    id UUID PRIMARY KEY,
    referencia VARCHAR(255),
    carteira_id_origem UUID,
    carteira_id_destino UUID,
    chave_pix VARCHAR(255),
    valor NUMERIC(19, 2),
    data_de_criacao TIMESTAMP,
    data_de_atualizacao TIMESTAMP,
    status VARCHAR(50)
);

-- Eventos de webhook Pix
CREATE TABLE IF NOT EXISTS public.evento_pix_webhook (
    id UUID PRIMARY KEY,
    evento_id VARCHAR(255) NOT NULL,
    end_to_end_id VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    data_de_ocorrencia TIMESTAMP NOT NULL,
    data_de_recebimento TIMESTAMP NOT NULL
);

-- Registro de idempotência
CREATE TABLE IF NOT EXISTS public.registro_idempotencia (
    id UUID PRIMARY KEY,
    chave_idempotencia VARCHAR(255) NOT NULL,
    referencia VARCHAR(255),
    data_de_criacao TIMESTAMP NOT NULL,
    payload_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uk_chave_idempotencia UNIQUE (chave_idempotencia)
);



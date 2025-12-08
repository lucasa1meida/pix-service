-- Reset de dados para testes da CarteiraController
DELETE FROM public.historico_transacao;
DELETE FROM public.carteira;

-- Carteira base para operações de depósito/saque/saldo
INSERT INTO public.carteira (id, ordem_id, saldo, data_de_criacao, data_de_atualizacao)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'proprietario-teste',
    100.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Histórico de transação para cenário de saldo histórico
DELETE FROM public.historico_transacao;
INSERT INTO public.historico_transacao (id, carteira_id, valor, tipo_transacao, natureza_transacao, end_to_end_id, data_transacao)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    150.00,
    'DEPOSITO',
    'CREDITO',
    NULL,
    TIMESTAMP '2025-01-01 10:00:00'
);



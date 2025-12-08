-- Reset de dados para testes da PixController
DELETE FROM public.evento_pix_webhook;
DELETE FROM public.registro_idempotencia;
DELETE FROM public.transferencia_pix;
DELETE FROM public.chave_pix;
DELETE FROM public.historico_transacao;
DELETE FROM public.carteira;

-- Carteiras de origem e destino
INSERT INTO public.carteira (id, ordem_id, saldo, data_de_criacao, data_de_atualizacao)
VALUES
(
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'proprietario-origem',
    500.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'proprietario-destino',
    200.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Chave Pix associada à carteira de destino
INSERT INTO public.chave_pix (id, carteira_id, tipo_chave_pix, valor_chave, ativo, data_de_criacao, data_de_atualizacao)
VALUES (
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'EMAIL',
    'destino@example.com',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);



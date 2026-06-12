-- Proprietario do arquivo: quem fez o upload. Usado para validar a delecao
-- (so o dono pode apagar). IF NOT EXISTS evita conflito com o ddl-auto=update
-- do dev, que pode ter criado a coluna a partir da entidade.
ALTER TABLE arquivo_midia
    ADD COLUMN IF NOT EXISTS proprietario_id VARCHAR(255);

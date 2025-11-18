CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS eventos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_evento TIMESTAMP NOT NULL,
    local_evento VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    imagem_path VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVO',
    organizador_id INT,
    FOREIGN KEY (organizador_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS participante (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    evento_id INT,
    UNIQUE(email, evento_id),
    FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE
);

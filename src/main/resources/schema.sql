CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS eventos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    data_evento DATETIME NOT NULL,
    local_evento TEXT NOT NULL,
    descricao TEXT,
    imagem_path TEXT,
    status TEXT NOT NULL DEFAULT 'ATIVO',
    organizador_id INTEGER,
    FOREIGN KEY (organizador_id) REFERENCES usuarios(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS participante (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    email TEXT NOT NULL,
    evento_id INTEGER,
    UNIQUE(email, evento_id),
    FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE
    );
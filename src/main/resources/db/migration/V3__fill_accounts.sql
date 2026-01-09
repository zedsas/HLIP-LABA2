INSERT INTO accounts (username, salt, password_hash, created_at)
VALUES ('player', 'gameSalt', '${playerHash}', CURRENT_TIMESTAMP);

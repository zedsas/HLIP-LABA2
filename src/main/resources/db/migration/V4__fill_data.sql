INSERT INTO resources (title, capacity, parent_id, created_at)
VALUES ('system', 100, NULL, CURRENT_TIMESTAMP);

INSERT INTO resources (title, capacity, parent_id, created_at)
VALUES ('data', 50, (SELECT id FROM resources WHERE title = 'system'), CURRENT_TIMESTAMP);

INSERT INTO resources (title, capacity, parent_id, created_at)
VALUES ('logs', 20, (SELECT id FROM resources WHERE title = 'data'), CURRENT_TIMESTAMP);

INSERT INTO resources (title, capacity, parent_id, created_at)
VALUES ('config', 10, (SELECT id FROM resources WHERE title = 'logs'), CURRENT_TIMESTAMP);

INSERT INTO permissions (user_id, resource_id, operation, created_at)
VALUES (
    (SELECT id FROM accounts WHERE username = 'player'),
    (SELECT id FROM resources WHERE title = 'data'),
    'READ',
    CURRENT_TIMESTAMP
);

INSERT INTO permissions (user_id, resource_id, operation, created_at)
VALUES (
    (SELECT id FROM accounts WHERE username = 'player'),
    (SELECT id FROM resources WHERE title = 'logs'),
    'WRITE',
    CURRENT_TIMESTAMP
);

INSERT INTO permissions (user_id, resource_id, operation, created_at)
VALUES (
    (SELECT id FROM accounts WHERE username = 'player'),
    (SELECT id FROM resources WHERE title = 'config'),
    'EXECUTE',
    CURRENT_TIMESTAMP
);

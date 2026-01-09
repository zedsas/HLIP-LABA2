ALTER TABLE resources
    ADD CONSTRAINT fk_resources_parent
        FOREIGN KEY (parent_id) REFERENCES resources(id);

ALTER TABLE permissions
    ADD CONSTRAINT fk_perm_user
        FOREIGN KEY (user_id) REFERENCES accounts(id);

ALTER TABLE permissions
    ADD CONSTRAINT fk_perm_res
        FOREIGN KEY (resource_id) REFERENCES resources(id);

CREATE INDEX idx_resources_parent_id ON resources(parent_id);
CREATE INDEX idx_permissions_user_id ON permissions(user_id);
CREATE INDEX idx_permissions_resource_id ON permissions(resource_id);

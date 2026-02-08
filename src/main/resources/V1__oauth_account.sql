CREATE TABLE oauth_account (
    id                  VARCHAR(36) PRIMARY KEY,
    provider_user_id    VARCHAR(255) NOT NULL,
    principal_id        VARCHAR(255) NOT NULL,
    provider_id         VARCHAR(100) NOT NULL,

    access_token_enc    TEXT NOT NULL,
    refresh_token_enc   TEXT,

    token_expiry        TIMESTAMP NOT NULL,

    scopes_json         JSONB NOT NULL,   -- ✅ NEW

    status              VARCHAR(50) NOT NULL,

    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,

    CONSTRAINT uq_oauth_account UNIQUE (principal_id, provider_id, provider_user_id)
);

CREATE INDEX ix_principal_provider
    ON oauth_account (principal_id, provider_id);

CREATE INDEX ix_provider_user
    ON oauth_account (provider_id, provider_user_id);
CREATE TABLE "oauth_access_tokens" (
  "id"               BIGSERIAL PRIMARY KEY
  , "account_id"       BIGSERIAL NOT NULL
  , "oauth_client_id"  BIGSERIAL NOT NULL
  , "access_token"     VARCHAR NOT NULL
  , "refresh_token"    VARCHAR NOT NULL
  , "createdAt"        TIMESTAMP default now() NOT NULL
);

ALTER TABLE "oauth_access_tokens" ADD CONSTRAINT "oauth_access_token_account_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON UPDATE RESTRICT ON DELETE CASCADE;

ALTER TABLE "oauth_access_tokens" ADD CONSTRAINT "oauth_access_token_client_fk" FOREIGN KEY ("oauth_client_id") REFERENCES "oauth_clients" ("id") ON UPDATE RESTRICT ON DELETE CASCADE;
CREATE TABLE "oauth_clients" (
  "id"              BIGSERIAL PRIMARY KEY
  , "owner_id"        BIGSERIAL NOT NULL
  , "grant_type"      VARCHAR NOT NULL
  , "client_id"       VARCHAR NOT NULL
  , "client_secret"   VARCHAR NOT NULL
  , "redirect_uri"    VARCHAR NOT NULL
  , "createdAt"       TIMESTAMP default now() NOT NULL
);

ALTER TABLE "oauth_clients" ADD CONSTRAINT "oauth_client_account_fk" FOREIGN KEY ("owner_id") REFERENCES "accounts" ("id") ON UPDATE RESTRICT ON DELETE CASCADE;
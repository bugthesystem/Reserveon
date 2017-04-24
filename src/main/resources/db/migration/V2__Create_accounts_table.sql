CREATE TABLE "accounts" (
  "id"              BIGSERIAL PRIMARY KEY
  , "email"           VARCHAR NOT NULL
  , "password"        VARCHAR NOT NULL
  , "createdAt"       TIMESTAMP default now() NOT NULL
);
CREATE TABLE "movies" (
  "id"              BIGSERIAL PRIMARY KEY
  , "imdbId"          VARCHAR NOT NULL UNIQUE
  , "availableSeats"  INT NOT NULL
  , "screenId"        VARCHAR NOT NULL
  , "movieTitle"      VARCHAR NOT NULL
  , "createdAt"       TIMESTAMP default now() NOT NULL
);
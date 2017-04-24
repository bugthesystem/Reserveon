Reserveon
=========

Sample reactive Movie Ticket reservation system

## Tech stack
 - [Scala](https://www.scala-lang.org/)
 - [Akka Http](https://github.com/akka/akka-http)
 - [Slick](https://github.com/slick/slick)
 - [Flayway](https://github.com/flyway/flyway) (for schema migration)
 - [Postgres](https://github.com/postgres/postgres)
 - [Redis](https://github.com/antirez/redis)
 - [ScalaTest](http://www.scalatest.org/)
 - [Specs2](https://github.com/etorreborre/specs2)
 - [Mockito](https://github.com/mockito/mockito)

## Implemented Features:
- OAuth2 support (client_credentials, access_token, refresh_token flows)
- CORS support
- Movie & Reservation CRUD
- Simple reservation mechanism
- Database schema migration
- Route tests

## Commands
### Run
**Environment variables**

`DB_PG_URL`  - db url by scheme jdbc:postgresql://host:port/db
`DB_PG_USER` - db user
`DB_PG_PWD`  - db password
`DB_CREATE_SAMPLE_DATA`  - enable or disable to create sample data (credential, token etc)

**_Sample run command_**
```sh
DB_PG_URL=jdbc:postgresql://localhost:65432/postgres \
DB_PG_USER=postgres \
DB_PG_PWD=s3cret \
DB_CREATE_SAMPLE_DATA=true \
sbt run
```

**OR**

Run `docker-compose`, it will start api, redis and postgres and will expose api port to host.
```sh
docker-compose up
```

### Create executable
```sh
sbt packageBin
```

### Test
```sh
sbt test
```

**P.S** [**Postman Rest Client**](https://www.getpostman.com/) collections also provided for manual testing in `postman` folder.

### Coverage
```sh
sbt clean coverage test
```

**To create coverage report**
```sh
sbt coverageReport
```

## Improvements
 - Store security tokens in cache (e.g redis) to decrease load on DB
 - Produce better error/validation messages from API
 - API Documentation using swagger or similar tool/lib
 - Serve data as paged and use cache (implement cache invalidation etc)
 - Implement async data processing queue or similar
 - Integration Testing
  1. DB integration tests using [`embedded postgres`](https://github.com/yandex-qatools/postgresql-embedded) or similar tool/lib
  2. Redis integration tests using [`embedded redis`](https://github.com/kstyrc/embedded-redis) or similar tool/lib


ziλasal.

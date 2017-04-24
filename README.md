Reserveon
=========

Sample reactive Movie Ticket reservation system  
[![Build Status](https://travis-ci.org/ziyasal/Reserveon.svg?branch=master)](https://travis-ci.org/ziyasal/Reserveon)

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
- Simple reservation mechanism using Redis
- Database schema migration
- Route tests

## Commands
### Run
:warning: _If you want to use docker-compose, you can skip manual steps._

#### Setup Postgres Database using Docker
**Run Postgres container**  
```sh
export DB_PG_PWD=s3cret
docker run --name pg-reserveon -e POSTGRES_PASSWORD=$DB_PG_PWD -p 65432:5432 -v /var/lib/postgresql/data -d postgres
```
**Connect to postgres:**  
```sh
docker run -it --link pg-reserveon:postgres --rm postgres \ 
sh -c 'exec psql -h "$POSTGRES_PORT_5432_TCP_ADDR" -p "$POSTGRES_PORT_5432_TCP_PORT" -U postgres'
```
**Create Database**  
```sh
CREATE DATABASE "reserveon";
```

#### Setup Redis using Docker
**To Run redis container;**  
```sh
docker run --name redis-reserveon -d -p 6379:6379 redis
```

**Environment variables**  
- `DB_PG_URL`  - db url by scheme jdbc:postgresql://host:port/db  
- `DB_PG_USER` - db user  
- `DB_PG_PWD`  - db password  
- `DB_CREATE_SAMPLE_DATA`  - enable or disable to create sample data (credential, token etc)  
- `REDIS_HOST`  - redis host  
- `REDIS_PORT`  - redis port  

**_Sample run command_**
```sh
DB_PG_URL=jdbc:postgresql://localhost:65432/reserveon \
DB_PG_USER=postgres \
DB_PG_PWD=s3cret \
DB_CREATE_SAMPLE_DATA=true \
REDIS_HOST=localhost \
REDIS_PORT=6379
sbt run
```

**OR**

Run `docker-compose`, it will start `api`, `redis` and `postgres` and will expose api port to host.  
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

## Improvements / TODO
 - Store security tokens in cache (e.g redis) to decrease load on DB
 - Produce better error/validation messages from API
 - API Documentation using swagger or similar tool/lib
 - Persist reservation data to DB to use for reports etc later on (currently stored in Redis)
 - Serve data as paged and implement data caching (implement cache invalidation etc)
 - Integration Testing
   1. DB integration tests using [`embedded postgres`](https://github.com/yandex-qatools/postgresql-embedded) or similar tool/lib
   2. Redis integration tests using [`embedded redis`](https://github.com/kstyrc/embedded-redis) or similar tool/lib
 - Use JWT authentication protocol with OAuth2 authentication framework


ziλasal.

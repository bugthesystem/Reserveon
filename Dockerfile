FROM fabric8/java-alpine-openjdk8-jdk

ENTRYPOINT ["java" ,"-jar", "/app/app.jar"]

ADD ./target/scala-2.12/reserveon_2.12-1.0.jar /app/app.jar
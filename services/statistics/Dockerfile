# syntax=docker/dockerfile:1

FROM sbtscala/scala-sbt:eclipse-temurin-jammy-19.0.1_10_1.8.2_2.13.10

WORKDIR /statistics

EXPOSE 8080

COPY . .
CMD [ "sbt", "run" ]
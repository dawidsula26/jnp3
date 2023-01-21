# syntax=docker/dockerfile:1

FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1

WORKDIR /scala-docker

EXPOSE 8080

COPY . .
CMD [ "sbt", "run" ]
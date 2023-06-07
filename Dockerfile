FROM ghcr.io/graalvm/jdk:ol9-java17 AS build

RUN microdnf install -y git gcc glibc-devel libstdc++-devel zlib-devel

WORKDIR /app

COPY . ./
RUN git fetch --unshallow || echo "Nothing to do"
RUN ./gradlew build -Dquarkus.package.type=native

FROM oraclelinux:9-slim AS run

ENV LANGUAGE="en_US:en"

COPY --from=build --chown=nobody /app/build/*-runner /deployments/application

EXPOSE 8080
USER nobody
CMD /deployments/application -Dquarkus.http.host=0.0.0.0

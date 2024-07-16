FROM node:lts AS build-emails

RUN apt-get update && apt-get install -y make
WORKDIR /app

COPY . ./
RUN make emails


FROM ghcr.io/graalvm/native-image-community:21-muslib AS build-code

ARG APP_STORAGE_TYPE
ENV APP_STORAGE_TYPE=$APP_STORAGE_TYPE

RUN microdnf install -y git findutils
WORKDIR /app

COPY --from=build-emails /app/ /app
RUN git fetch --unshallow || echo "Nothing to do"
RUN ./gradlew --no-daemon --exclude-task test build -Dquarkus.package.jar.enabled=false -Dquarkus.native.enabled=true -Dquarkus.native.additional-build-args=--static,--libc=musl


FROM scratch AS run

ENV LANGUAGE="en_US:en"

COPY --from=build-code /app/build/*-runner /application

EXPOSE 8080
CMD ["/application", "-Dquarkus.http.host=0.0.0.0", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager"]

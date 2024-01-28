FROM node:lts AS build-emails

RUN apt-get update && apt-get install -y make
WORKDIR /app

COPY . ./
RUN make emails


FROM eclipse-temurin:21-jdk AS build-code

ARG APP_STORAGE_TYPE
ENV APP_STORAGE_TYPE=$APP_STORAGE_TYPE

RUN apt-get update && apt-get install -y git
WORKDIR /app

COPY --from=build-emails /app/ /app
RUN git fetch --unshallow || echo "Nothing to do"
RUN ./gradlew --no-daemon --exclude-task test build


FROM eclipse-temurin:21-jre AS run

ENV LANGUAGE="en_US:en"
ENV JAVA_OPTS="$JAVA_OPTS -Dquarkus.http.host=0.0.0.0"
ENV JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

COPY --from=build-code --chown=nobody /app/build/quarkus-app/lib/ /deployments/lib
COPY --from=build-code --chown=nobody /app/build/quarkus-app/*.jar /deployments/
COPY --from=build-code --chown=nobody /app/build/quarkus-app/app/ /deployments/app
COPY --from=build-code --chown=nobody /app/build/quarkus-app/quarkus/ /deployments/quarkus

EXPOSE 8080
USER nobody
CMD java -jar /deployments/quarkus-run.jar

FROM eclipse-temurin:17-jdk AS build

RUN apt-get update && apt-get install -y git

WORKDIR /app

COPY . ./
RUN git fetch --unshallow || echo "Nothing to do"
RUN ./gradlew build

FROM eclipse-temurin:17-jre AS run

ENV LANGUAGE="en_US:en"
ENV JAVA_OPTS="$JAVA_OPTS -Dquarkus.http.host=0.0.0.0"
ENV JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

COPY --from=build --chown=nobody /app/build/quarkus-app/lib/ /deployments/lib
COPY --from=build --chown=nobody /app/build/quarkus-app/*.jar /deployments/
COPY --from=build --chown=nobody /app/build/quarkus-app/app/ /deployments/app
COPY --from=build --chown=nobody /app/build/quarkus-app/quarkus/ /deployments/quarkus

EXPOSE 8080
USER nobody
CMD java -jar /deployments/quarkus-run.jar

FROM bellsoft/liberica-openjdk-alpine:21 as builder
WORKDIR /application
ARG JAR_FILE
COPY target/${JAR_FILE}.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM bellsoft/liberica-openjdk-alpine:21
ARG VERSION
ARG GIT_SHA
LABEL VERSION=$VERSION
LABEL GIT_SHA=$GIT_SHA
ENV VERSION=$VERSION
ENV GIT_SHA=$GIT_SHA

WORKDIR /application
COPY run-app.sh run-app.sh
RUN chmod +x run-app.sh

COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

ENTRYPOINT ./run-app.sh

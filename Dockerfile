FROM openjdk:8-jdk-alpine as build
MAINTAINER https://www.netshoes.com.br
WORKDIR /workspace/app

COPY app/target/athena-dependency-analyzer.jar /workspace/app
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../../*.jar)

FROM openjdk:11-jre-slim
MAINTAINER https://www.netshoes.com.br

ARG DEPENDENCY=/workspace/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /opt/app/lib
COPY --from=build ${DEPENDENCY}/META-INF /opt/app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /opt/app
COPY run.sh /opt/app/run.sh

RUN chmod +x /opt/app/run.sh
EXPOSE 8080
ENTRYPOINT /opt/app/run.sh $MONGO_URI $RABBITMQ_ADDRESSES $RABBITMQ_HOST $RABBITMQ_USER $RABBITMQ_PASS $GITHUB_TOKEN $GITHUB_HOST $GITHUB_ORGANIZATION $ADMIN_USERNAME $ADMIN_PASSWORD

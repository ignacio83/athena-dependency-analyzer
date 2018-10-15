FROM openjdk:11-jre-slim

MAINTAINER https://www.netshoes.com.br

COPY run.sh /opt/run.sh
RUN chmod +x /opt/run.sh
COPY app/target/athena-dependency-analyzer.jar /opt/athena-dependency-analyzer.jar
EXPOSE 8080
ENTRYPOINT /opt/run.sh $MONGO_URI $RABBITMQ_ADDRESSES $RABBITMQ_HOST $RABBITMQ_USER $RABBITMQ_PASS $GITHUB_TOKEN $GITHUB_HOST $GITHUB_ORGANIZATION $ADMIN_USERNAME $ADMIN_PASSWORD

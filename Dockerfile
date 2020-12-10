FROM adoptopenjdk/openjdk11:jdk-11.0.9.1_1-alpine-slim

EXPOSE 8080

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar

ADD build/libs/kube-dashboard-0.0.1-SNAPSHOT.jar app.jar
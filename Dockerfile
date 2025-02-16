FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/salessavvy-0.0.1-SNAPSHOT.jar salessavvy.jar

COPY src/main/resources/application.properties /app/config/application.properties

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "salessavvy.jar", "--spring.config.location=file:/app/config/application.properties"]
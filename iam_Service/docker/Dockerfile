FROM eclipse-temurin:21-jre

WORKDIR /srv

COPY target/*.jar app.jar

ENV PROFILE=local-idea

EXPOSE 8189

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${PROFILE}"]

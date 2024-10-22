FROM openjdk:21

WORKDIR /app

COPY target/spring-boot-test-0.0.1-SNAPSHOT.jar ./spring-boot-test-github-actions.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "spring-boot-test-github-actions.jar"]

# Run:
#   'docker build -t ivangorbunovv/spring-boot-test-github-actions-image .'

FROM openjdk:21
EXPOSE 8081
ADD target/Frequency-0.0.1-SNAPSHOT.jar FrequencyService.jar
ENTRYPOINT ["java", "-jar", "FrequencyService.jar"]
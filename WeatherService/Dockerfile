FROM openjdk:21
EXPOSE 8080
ADD target/Weather-0.0.1-SNAPSHOT.jar WeatherService.jar
COPY src/main/resources/airport_data.csv /src/main/resources/
ENTRYPOINT ["java", "-jar", "WeatherService.jar"]
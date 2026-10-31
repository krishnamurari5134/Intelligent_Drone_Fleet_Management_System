FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src ./src
RUN sed -i 's/"127.0.0.1"/"0.0.0.0"/' src/drone/WebServer.java && sed -i 's/WebServer.start(8080)/WebServer.start(Integer.parseInt(System.getenv().getOrDefault("PORT","8080")))/' src/drone/Main.java
RUN mkdir out && javac -d out src/drone/*.java && cp -r src/web out/web && jar cfe DroneFleet.jar drone.Main -C out .

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/DroneFleet.jar .
CMD ["java", "-Djava.awt.headless=true", "-jar", "DroneFleet.jar", "--web"]
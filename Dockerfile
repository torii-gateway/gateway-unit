# Use a smaller, JDK 17-based image for running the app
FROM openjdk:17-jdk-slim

# The application's jar file
ARG JAR_FILE=target/*.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/app.jar"]
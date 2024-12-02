# Use the official OpenJDK base image with Java 17
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/jaguar.jar /app/jaguar.jar

# Expose the port that your Spring Booot application will run on
EXPOSE 8090

# Specify the command to run on container startup
CMD ["java", "-jar", "jaguar.jar"]
# Build stage
#1) tells docker to start with maven and java 17 already installed. name the stage as BUILD
#2) create a app folder 3) copy all source code from computer to container
#4) use mvn clean package to compile ur code and package into .jar file 
FROM maven:3.8.4-openjdk-17 AS build 
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
#1) tell docker to start with java 17 only and so only contains whats needed to run java
#2)create a app folder 3) it reaches back to build stage and graps only jar file to create new image
#4) app listens to port 8080 5) command to run when container launches runs java jar file
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
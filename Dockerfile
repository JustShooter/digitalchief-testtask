FROM maven:3.9.2 AS build
WORKDIR /opt/chief/
COPY . .
RUN mvn clean package



FROM openjdk:17-alpine
COPY --from=build /opt/chief/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]

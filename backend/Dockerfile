FROM maven:3.8.4-openjdk-17 AS build-stage

COPY . /app
WORKDIR /app
RUN mvn package

FROM maven:3.8.4-openjdk-17 AS production-stage
COPY --from=build-stage /app/target/*.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]

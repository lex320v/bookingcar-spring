FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY pom.xml .
COPY lombok.config .
COPY .mvn .mvn
COPY src src

RUN ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/bookingcar-*.jar ./bookingcar.jar
COPY src/main/resources/application-docker.yaml .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "bookingcar.jar"]
CMD ["--spring.config.location=file:application-docker.yaml"]

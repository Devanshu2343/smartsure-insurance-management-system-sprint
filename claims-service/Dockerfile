FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY config-server/pom.xml config-server/pom.xml
COPY claims-service/pom.xml claims-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY policy-service/pom.xml policy-service/pom.xml
COPY admin-report-service/pom.xml admin-report-service/pom.xml

COPY claims-service/src claims-service/src
RUN mvn -pl claims-service -am clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=3

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/claims-service/target/claims-service-1.0.0.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

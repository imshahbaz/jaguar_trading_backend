FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/jaguar_trading_backend.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8090
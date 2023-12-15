FROM openjdk:17-jdk-slim-buster
EXPOSE 8090
ADD target/jaguar_trading_backend.jar jaguar_trading_backend.jar
ENTRYPOINT ["java","-jar","/jaguar_trading_backend.jar"]
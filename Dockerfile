FROM openjdk:8-jdk-alpine
EXPOSE 8200
ADD target/company-service.jar company-service.jar
ENTRYPOINT ["java","-jar","/company-service.jar"]
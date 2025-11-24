FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY target/auth-service-1.0.0.jar /app/auth-service.jar
COPY private.pem /app/private.pem
COPY jwks.json /app/jwks.json

EXPOSE 3000

CMD ["java","-jar","/app/auth-service.jar"]
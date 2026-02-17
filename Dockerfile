# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/plotter-pro-backend-0.0.1-SNAPSHOT.jar app.jar
# Use JAVA_OPTS to control memory usage (Critical for free tiers)
ENV JAVA_OPTS="-Xmx350m -Xms350m -Djava.net.preferIPv4Stack=true -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8082
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8082}"]

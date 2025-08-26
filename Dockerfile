# -------- STAGE 1: Build the JAR --------
FROM eclipse-temurin:21-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

# -------- STAGE 2: Run the JAR --------
FROM eclipse-temurin:21-jdk-alpine

ENV PORT=8080
EXPOSE 8080

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

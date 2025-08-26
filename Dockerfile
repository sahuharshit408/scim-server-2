# -------- STAGE 1: Build the JAR --------
    FROM eclipse-temurin:21-jdk AS builder

    WORKDIR /app
    COPY . .
    
    # Use Maven wrapper if available, fallback to system mvn
    RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests
    
    # -------- STAGE 2: Run the App --------
    FROM eclipse-temurin:21-jdk-alpine
    
    ENV PORT=8080
    EXPOSE 8080
    
    WORKDIR /app
    
    # Copy the built jar from stage 1
    COPY --from=builder /app/target/*.jar app.jar
    
    ENTRYPOINT ["java", "-jar", "app.jar"]
    
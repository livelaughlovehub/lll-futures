# Dockerfile for LL&L Futures Backend
FROM maven:3.8.6-eclipse-temurin-17

# Set working directory
WORKDIR /app

# Copy pom.xml first (for better caching)
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV PORT=8080

# Run the application
CMD ["java", "-jar", "target/futures-backend-1.0.0.jar"]
# Dockerfile for LL&L Futures Backend
FROM maven:3.8.6-eclipse-temurin-21

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

# Create uploads directory for local storage fallback
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Set environment variables for production
ENV SPRING_PROFILES_ACTIVE=production
ENV PORT=8080
ENV STORAGE_TYPE=r2

# Run the application
CMD ["java", "-jar", "target/futures-backend-1.0.0.jar"]
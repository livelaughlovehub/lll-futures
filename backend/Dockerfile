# Dockerfile for LL&L Futures Backend

# Stage 1: Build
FROM maven:3.9.9-amazoncorretto-21 AS build

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

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/futures-backend-1.0.0.jar app.jar

# Create uploads directory for local storage fallback
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Set environment variables for production
ENV SPRING_PROFILES_ACTIVE=production
ENV PORT=8080
ENV STORAGE_TYPE=r2

# Neon Database Configuration
ENV NEON_DATABASE_URL=jdbc:postgresql://ep-rapid-fire-adkw7qfx-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require
ENV NEON_USERNAME=neondb_owner
ENV NEON_PASSWORD=npg_PCkmguB9nyL8

# Cloudflare R2 Storage Configuration
ENV R2_ENDPOINT=https://feb9499d5178b38db5b7dfc7b6299c3b.r2.cloudflarestorage.com
ENV R2_ACCESS_KEY=3994b90cc48129c16ae5e518a028b43f
ENV R2_SECRET_KEY=d81fe62a5170aafaef2579e1970e758d581dc7d3f119896dedab00e408e0b25f
ENV R2_BUCKET=lllfutures
ENV R2_REGION=auto

# Run the application
CMD ["java", "-jar", "app.jar"]
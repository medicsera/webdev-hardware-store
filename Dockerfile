FROM eclipse-temurin:21-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY frontend/pom.xml frontend/pom.xml

# Download dependencies (cached layer) — retry for transient network errors
RUN ./mvnw -DskipTests dependency:go-offline || \
    (sleep 15 && ./mvnw -DskipTests dependency:go-offline) || \
    (sleep 30 && ./mvnw -DskipTests dependency:go-offline)

# Copy backend source only
COPY backend backend

# Build backend JAR
RUN ./mvnw -DskipTests -pl backend clean package

# Runtime stage
FROM eclipse-temurin:21-alpine
WORKDIR /app
EXPOSE 8080

COPY --from=build /app/backend/target/*.jar app.jar

RUN addgroup -S app && adduser -S -G app appuser && \
    mkdir -p /app/uploads/products && \
    chown -R appuser:app /app/uploads

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

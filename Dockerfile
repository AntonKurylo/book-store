# ========== BUILD STAGE ==========
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy Maven wrapper, pom.xml and config first (for caching dependencies)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY checkstyle.xml .

# Download dependencies once, cached by Docker
RUN ./mvnw -B -q dependency:go-offline

# Copy source code
COPY src src

# Build Spring Boot layered JAR (only runs when src changes)
RUN ./mvnw -B -DskipTests package


# ========== EXTRACT LAYERS ==========
FROM eclipse-temurin:17-jre-jammy AS extractor
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract


# ========== RUNTIME IMAGE ==========
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy Spring Boot layers
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

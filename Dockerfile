# Stage 1: Build dependencies and modules
FROM maven:3.9.9-eclipse-temurin-17 as builder

WORKDIR /app

# 1. Copy only POM files first (to cache dependencies)
COPY pom.xml .
COPY common/pom.xml ./common/
COPY bot/pom.xml ./bot/
COPY scrapper/pom.xml ./scrapper/

# 2. Download dependencies (without source code)
RUN mvn -B dependency:go-offline

# 3. Copy source code
COPY common/src ./common/src
COPY bot/src ./bot/src
COPY scrapper/src ./scrapper/src

# 4. Build all modules (including common)
RUN mvn -B clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/bot/target/*.jar ./bot.jar
COPY --from=builder /app/scrapper/target/*.jar ./scrapper.jar

CMD ["sh", "-c", "java -jar bot.jar & java -jar scrapper.jar"]

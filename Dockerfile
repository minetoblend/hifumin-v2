FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
RUN ./gradlew --version

COPY build.gradle.kts settings.gradle.kts .
RUN ./gradlew dependencies --no-daemon -q || true

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted


FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libfreetype6 libfontconfig1 libgl1 libx11-6 libxext6 libxrender1 \
    && rm -rf /var/lib/apt/lists/*

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

COPY --from=builder /build/extracted/dependencies ./
COPY --from=builder /build/extracted/spring-boot-loader ./
COPY --from=builder /build/extracted/snapshot-dependencies ./
COPY --from=builder /build/extracted/application ./

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "org.springframework.boot.loader.launch.JarLauncher"]

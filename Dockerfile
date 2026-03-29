FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
RUN ./gradlew --version

COPY build.gradle.kts settings.gradle.kts .
RUN ./gradlew dependencies --no-daemon -q || true

COPY src src
ENV DOCKER_BUILD=1
RUN ./gradlew bootJar --no-daemon -x test

RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted


FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

COPY --from=builder /build/extracted/dependencies ./
COPY --from=builder /build/extracted/spring-boot-loader ./
COPY --from=builder /build/extracted/snapshot-dependencies ./
COPY --from=builder /build/extracted/application ./

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "org.springframework.boot.loader.launch.JarLauncher"]

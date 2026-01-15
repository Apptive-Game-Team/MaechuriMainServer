FROM --platform=linux/arm64 gradle:9.2-jdk21 AS build
WORKDIR /app

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

COPY build.gradle settings.gradle ./
RUN GITHUB_USERNAME=${GITHUB_USERNAME} GITHUB_TOKEN=${GITHUB_TOKEN} gradle dependencies --no-daemon

COPY . .
RUN gradle clean bootJar -x test --no-daemon

FROM --platform=linux/arm64 eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
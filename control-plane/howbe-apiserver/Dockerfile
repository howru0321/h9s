FROM gradle:8.8-jdk17 AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./

RUN gradle dependencies --no-daemon

COPY . .

RUN gradle clean build --no-daemon

FROM openjdk:17

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/howbe-apiserver.jar

ENTRYPOINT ["java"]
CMD ["-jar", "howbe-apiserver.jar"]

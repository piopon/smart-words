FROM hseeberger/scala-sbt:17.0.1_1.6.2_2.13.6 AS build

WORKDIR /workspace

COPY build.sbt ./
COPY project ./project
RUN sbt --batch update

COPY src ./src
RUN sbt --batch assembly

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/target/scala-2.13/service-quiz-0.1.jar /app/service-quiz.jar

ENV QUIZ_DATA_DIR=/app/data

RUN mkdir -p /app/data

EXPOSE 2222

ENTRYPOINT ["java", "-jar", "/app/service-quiz.jar"]

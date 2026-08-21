FROM hseeberger/scala-sbt:eclipse-temurin-17.0.2_1.6.2_2.13.8 AS build

WORKDIR /workspace

COPY build.sbt ./
COPY project ./project
RUN sbt --batch update

COPY src ./src
RUN sbt --batch assembly

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/target/scala-2.13/service-word-0.1.jar /app/service-word.jar

ENV WORD_DATA_DIR=/app/data

RUN mkdir -p /app/data

EXPOSE 1111

CMD ["java", "-jar", "/app/service-word.jar"]

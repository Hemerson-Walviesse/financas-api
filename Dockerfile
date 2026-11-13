# ---------- Etapa 1: compila o projeto ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia primeiro só o que define as dependências (aproveita o cache do Docker)
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B -DskipTests package && cp target/*.jar app.jar

# ---------- Etapa 2: imagem final, só com o Java de execução ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar

# O plano grátis do Render tem 512 MB: limita a memória da JVM
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Copiamos solo el codigo fuente
COPY Libreria/src/ ./src/

# Compilamos todos los .java y dejamos los .class en la carpeta out
RUN mkdir out && javac -encoding UTF-8 -d out src/*.java

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Solo llevamos los .class compilados (imagen mas liviana)
COPY --from=build /build/out ./

# Para que las tildes y la ñ se vean bien
ENV LANG=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Aqui se guardan libros.txt y clientes.txt (Persistencia escribe en "datos/")
VOLUME /app/datos

CMD ["java", "-cp", ".", "Main"]

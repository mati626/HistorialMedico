#imagen con Android SDK y varios JDK ya instalados
FROM mingc/android-build-box:latest

#la imagen no trae la plataforma 37 que usa compileSdk
RUN sdkmanager "platforms;android-37.0"

#carpeta de trabajo dentro del contenedor
WORKDIR /app

#copia el proyecto, sin lo que excluye .dockerignore
COPY . .

#mismo paso que el CI
RUN chmod +x gradlew

#mismo comando que el CI
RUN ./gradlew assembleDebug
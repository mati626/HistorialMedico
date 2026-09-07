# Aplicación Móvil para Android de Registro Personal de Historial Médico

Aplicación móvil diseñada para el registro, organización y seguimiento de la información de salud personal y de personas a cargo (hijos o adultos mayores) de manera centralizada y privada.

## Stack Tecnológico

- **Kotlin** + Jetpack Compose (interfaz de usuario)
- **Room** - persistencia local (base de datos SQLite en el dispositivo)
- **BiometricPrompt** + **EncryptedSharedPreferences** - autenticación y seguridad local

## Arquitectura y Privacidad

Esta aplicación almacena toda la información de forma local en el dispositivo, sin backend ni servidor externo. Esta decisión corresponde a la Ley N° 19.628, modificada por la Ley N° 21.719, que clasifica los datos de salud como datos sensibles y exige medidas de seguridad reforzadas para su tratamiento. Al mantener los datos exclusivamente en el dispositivo del usuario, se evita el tratamiento de información sensible en servidores externos.

## Estrategia de Ramas (GitFlow)

Este proyecto sigue el modelo GitFlow:

- **main** - código estable
- **develop** - rama de integración, unión de módulos terminados
- **feature/autenticacion** - módulo de seguridad (biometría y PIN)
- **feature/historial-medico** - módulo de registro de historial médico
- **feature/alarmas-recordatorios** - módulo de notificaciomes
- **feature/exportacion** - módulo de exportacion/compartición de datos

## Como Correr el Proyecto

1. Clonar el repositorio
2. Abrir el proyecto en Android studio
3. Sincronizar Gradle (opcion: Sync Now)
4. Ejecutar en emulador o dispositivo físico con Android 7.0 (API 24) o superior

## Estado Actual

- **Autenticacion:** biometría y PIN funcionando, pendiente migrar a EncryptedSharedPreferences
- **Historial medico:** pendiente
- **Alarmas y recordatorios:** pendiente
- **Exportacion:** pendiente

## Licencia

Este proyecto usa licencia MIT, -ver archivo [LICENSE](LICENSE)

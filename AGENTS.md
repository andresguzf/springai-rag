# AGENTS.md - Contexto y Guía del Proyecto

Este documento proporciona una visión general técnica, la arquitectura y las pautas para agentes de IA y desarrolladores que trabajen en este proyecto.

## 📌 Descripción del Proyecto
`9-springai-rag` es una aplicación Java construida con **Spring Boot** e integrada con **Spring AI** para implementar técnicas de Retrieval-Augmented Generation (RAG) con:
- **LLM:** Ollama (`qwen3:4b`)
- **Embedding:** Nomic Embed Text (`nomic-embed-text`)
- **Vector Store:** Chroma VectorStore (`http://localhost:8000`)
- **Document Reader:** Reader de documentos PDF (`spring-ai-pdf-document-reader`)

---

## 🛠️ Tecnologías y Dependencias
- **Java:** 25
- **Spring Boot:** 4.1.0
- **Spring AI:** 2.0.0
  - `spring-ai-starter-model-ollama`
  - `spring-ai-starter-vector-store-chroma`
  - `spring-ai-pdf-document-reader`
- **Gestor de dependencias:** Maven (`mvnw`)
- **Infraestructura:** Docker (Contenedor ChromaDB)

---

## 🐳 Infraestructura VectorStore (ChromaDB en Docker)

El servicio de Vector Store utiliza **ChromaDB** ejecutándose en un contenedor Docker con persistencia de datos.

### Parámetros de la Infraestructura
- **Nombre del contenedor:** `chromadb`
- **Imagen oficial:** `chromadb/chroma`
- **Puerto:** `8000` (mapeado a `0.0.0.0:8000`)
- **Volumen persistente:** `chroma_data` montado en `/data` de la imagen
- **Política de reinicio:** `--restart always`

### 🚀 Comando Completo de Despliegue (`docker run`)

- **Crear volumen e iniciar contenedor (formato multilínea):**
  ```bash
  docker volume create chroma_data

  docker run -d \
    --name chromadb \
    --restart always \
    -p 8000:8000 \
    -v chroma_data:/data \
    chromadb/chroma
  ```

- **Comando en una sola línea:**
  ```bash
  docker run -d --name chromadb --restart always -p 8000:8000 -v chroma_data:/data chromadb/chroma
  ```

### 🛠️ Comandos de Administración de ChromaDB

- **Iniciar / Detener contenedor existente:**
  ```bash
  docker start chromadb
  docker stop chromadb
  ```
- **Verificar estado:**
  ```bash
  docker ps -f "name=chromadb"
  ```
- **Health Check de la API:**
  ```bash
  curl -s http://localhost:8000/api/v2/heartbeat
  ```

---

## 🎯 Skills Disponibles

El proyecto cuenta con las skills copiadas en `.agents/skills/`:

### 1. `spring-boot-best-practices`
- **Ubicación:** `.agents/skills/spring-boot-best-practices/SKILL.md`
- **Descripción:** Guía para la creación, refactorización y extensión de aplicaciones Spring Boot siguiendo arquitectura en capas limpia, mejores prácticas de desarrollo y estándares de Java moderno.
- **Trigger / Cuándo invocar:** Debe invocarse **SIEMPRE** que el usuario solicite crear una API de Spring Boot, un monolito Spring Web, o cuando se pida crear, agregar, refactorizar o modificar un `Entity` (model), `Repository`, `Service`, `Controller`, `DTO` o `Mapper`.

---

## 🏗️ Arquitectura y Estructura del Código

La aplicación sigue una arquitectura limpia de Spring Boot:

```
src/main/java/com/andres/course/agy/springboot/springairag/app/
└── Application.java
```

---

## ⚙️ Configuración (`application.properties`)

```properties
spring.application.name=9-springai-rag

# Ollama Model Configuration
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=qwen3:4b
spring.ai.ollama.embedding.model=nomic-embed-text

# Chroma VectorStore Configuration
spring.ai.vectorstore.chroma.client.host=http://localhost
spring.ai.vectorstore.chroma.client.port=8000
spring.ai.vectorstore.chroma.collection-name=springai_rag
spring.ai.vectorstore.chroma.initialize-schema=true
```

---

## 🚀 Comandos de Construcción y Verificación

- **Compilar el proyecto:**
  ```bash
  ./mvnw clean compile
  ```
- **Ejecutar la aplicación:**
  ```bash
  ./mvnw spring-boot:run
  ```

# 🤖 Spring AI RAG Application (`springai-agy-rag`)

Una aplicación moderna desarrollada con **Java 25**, **Spring Boot** y **Spring AI** para implementar técnicas de **Retrieval-Augmented Generation (RAG)** utilizando inteligencia artificial 100% local y soberanía de datos.

---

## 💡 ¿De qué trata esta aplicación? (Resumen para todo público)

**Spring AI RAG** es un asistente de IA capaz de leer y comprender documentos (como archivos PDF) para responder preguntas de manera precisa basándose en el contenido real de dichos documentos.

### 🌟 Beneficios Clave:
- **Privacidad y Seguridad:** Todo se procesa de forma local en tu máquina (sin enviar datos sensibles a servicios de IA en la nube).
- **Respuestas Confiables:** Reduce las "alucinaciones" de la IA al obligar al modelo a fundamentar sus respuestas en los documentos cargados.
- **Búsqueda Inteligente:** Encuentra información relevante no por palabras clave exactas, sino por el **significado conceptual** de lo que buscas.

---

## 🛠️ Resumen Técnico (Para Desarrolladores)

Esta aplicación utiliza la pila de componentes de **Spring AI 2.0.0** e integra un pipeline RAG completo:

```
[ Documento PDF ] ──> [ PDF Reader ] ──> [ Text Splitter ]
                                               │
                                               ▼
                                      [ Nomic Embed Text ]
                                               │
                                               ▼
                                   [ ChromaDB VectorStore ]
                                               │
  [ Pregunta Usuario ] ──> [ Retrieval ] ──────┘
                               │
                               ▼
                    [ Ollama (qwen3:4b) ] ──> [ Respuesta RAG ]
```

### 🧱 Arquitectura y Componentes:
- **Lenguaje & Framework:** Java 25 / Spring Boot 4.1.0
- **Integración de IA:** Spring AI 2.0.0 (`spring-ai-starter-model-ollama`, `spring-ai-starter-vector-store-chroma`)
- **Modelo de Lenguaje (LLM):** Ollama ejecutando `qwen3:4b`
- **Modelo de Embeddings:** `nomic-embed-text`
- **Base de Datos Vectorial:** ChromaDB v2 corriendo en contenedor Docker
- **Lector de Documentos:** `spring-ai-pdf-document-reader`

---

## 📋 Requisitos Previos

Asegúrate de contar con los siguientes elementos instalados en tu sistema:

1. **Java 25** (o JDK compatible)
2. **Docker Desktop** (para la base de datos vectorial ChromaDB)
3. **Ollama** (para los modelos de IA locales)

---

## 🚀 Guía de Inicio Rápido

### 1. Iniciar la Base de Datos Vectorial (ChromaDB)

Ejecuta el contenedor Docker de **ChromaDB** con volumen persistente:

```bash
docker volume create chroma_data

docker run -d \
  --name chromadb \
  --restart always \
  -p 8000:8000 \
  -v chroma_data:/data \
  chromadb/chroma
```

Verifica que el servicio esté respondiendo:
```bash
curl -s http://localhost:8000/api/v2/heartbeat
```

### 2. Descargar Modelos en Ollama

Asegúrate de que Ollama esté ejecutándose y descarga los modelos requeridos:

```bash
ollama pull qwen3:4b
ollama pull nomic-embed-text
```

### 3. Compilar y Ejecutar la Aplicación

Compila el proyecto con Maven Wrapper:
```bash
./mvnw clean compile
```

Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
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

## 📁 Estructura del Proyecto

```text
.
├── AGENTS.md                  # Contexto y pautas para agentes de IA y desarrolladores
├── README.md                  # Documentación principal del repositorio
├── pom.xml                    # Gestión de dependencias de Maven
├── src/
│   ├── main/
│   │   ├── java/com/andres/course/agy/springboot/springairag/app/
│   │   │   └── Application.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── mvnw / mvnw.cmd            # Wrapper de Maven
```

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**.

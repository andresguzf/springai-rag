package com.andres.course.agy.springboot.springairag.app.services.impl;

import com.andres.course.agy.springboot.springairag.app.dto.ChatRequestDto;
import com.andres.course.agy.springboot.springairag.app.dto.ChatResponseDto;
import com.andres.course.agy.springboot.springairag.app.dto.UploadResponseDto;
import com.andres.course.agy.springboot.springairag.app.services.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagServiceImpl(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public UploadResponseDto processAndStorePdf(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo proporcionado está vacío.");
        }

        try {
            Resource pdfResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
                }
            };

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                    pdfResource,
                    PdfDocumentReaderConfig.builder().build());

            List<Document> rawDocuments = pdfReader.get();

            TokenTextSplitter textSplitter = TokenTextSplitter.builder().build();
            List<Document> splitDocuments = textSplitter.apply(rawDocuments);

            // Almacenar documentos y sus embeddings en ChromaDB
            vectorStore.accept(splitDocuments);

            return new UploadResponseDto(
                    file.getOriginalFilename(),
                    rawDocuments.size(),
                    splitDocuments.size(),
                    "Documento PDF procesado, convertido a embeddings e indexado en ChromaDB correctamente.");
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatResponseDto askQuestion(ChatRequestDto request) {
        // 1. Buscar los documentos más relevantes en ChromaDB mediante distancia coseno / similitud
        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.message())
                .topK(4)
                .build();

        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

        if (similarDocs.isEmpty()) {
            return new ChatResponseDto(
                    "No se encontró información relevante en los documentos cargados.",
                    List.of());
        }

        String context = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Extracción detallada de metadatos (Nombre de archivo, Página y Fragmento)
        List<String> sources = similarDocs.stream()
                .map(doc -> {
                    Map<String, Object> meta = doc.getMetadata();
                    String fileName = "documento.pdf";
                    if (meta != null) {
                        if (meta.containsKey("file_name")) {
                            fileName = meta.get("file_name").toString();
                        } else if (meta.containsKey("pdf.file_name")) {
                            fileName = meta.get("pdf.file_name").toString();
                        } else if (meta.containsKey("input_file")) {
                            fileName = meta.get("input_file").toString();
                        }
                    }
                    Object pageNum = (meta != null && (meta.containsKey("page_number") || meta.containsKey("page_index")))
                            ? meta.getOrDefault("page_number", meta.get("page_index"))
                            : "N/A";

                    String snippet = doc.getText() != null
                            ? (doc.getText().length() > 180 ? doc.getText().substring(0, 180).trim() + "..." : doc.getText().trim())
                            : "";

                    return String.format("📄 Archivo: %s | 📌 Página: %s | 🔍 Extracto: %s", fileName, pageNum, snippet.replace("\n", " "));
                })
                .distinct()
                .collect(Collectors.toList());

        // 2. Definir System Prompt estricto exigiendo formateo Markdown explícito
        String systemMessage = """
                Eres un asistente experto en Inteligencia Artificial y consultoría RAG.
                Responde a la pregunta del usuario utilizando EXCLUSIVAMENTE la información contenida en el siguiente contexto recuperado de la base de datos vectorial.

                REGLAS ESTRICTAS DE RESPUESTA:
                1. Si la respuesta no se encuentra explícitamente en el contexto proporcionado, responde exactamente:
                   "No dispongo de la información solicitada en los documentos cargados."
                2. No utilices conocimientos previos externos ni inventes información.
                3. Formatea tu respuesta obligatoriamente utilizando sintaxis Markdown clara y estructurada:
                   - Usa títulos y subtítulos Markdown (`#`, `##`, `###`) para organizar las secciones.
                   - Usa listas con viñetas (`-` o `*`) o numeradas (`1.`, `2.`) para enumerar puntos, recomendaciones o actividades.
                   - Usa **negritas** para resaltar conceptos clave.
                   - Si el contexto incluye datos tabulares o listas complejas, organízalos en una tabla Markdown.
                4. Si el contexto incluye información turística o de viajes, organiza tu respuesta cubriendo cuando aplique:
                   - 📍 Lugares recomendados para visitar
                   - 🎯 Actividades turísticas sugeridas
                   - 🗓️ Mejor época o temporada para viajar
                   - 💡 Consejos prácticos basados en los documentos.

                INFORMACIÓN DE CONTEXTO RECUPERADA:
                {context}
                """;

        String answer = chatClient.prompt()
                .system(sp -> sp.text(systemMessage).param("context", context))
                .user(request.message())
                .call()
                .content();

        return new ChatResponseDto(answer, sources);
    }
}

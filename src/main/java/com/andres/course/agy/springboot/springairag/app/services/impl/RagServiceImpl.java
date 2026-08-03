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
        // 1. Buscar los documentos más relevantes en ChromaDB mediante distancia coseno
        // / similitud
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

        List<String> sources = similarDocs.stream()
                .map(doc -> doc.getMetadata() != null ? doc.getMetadata().toString() : "sin-metadatos")
                .distinct()
                .collect(Collectors.toList());

        // 2. Definir System Prompt estricto
        String systemMessage = """
                Eres un asistente experto de Inteligencia Artificial.
                Responde a la pregunta del usuario utilizando EXCLUSIVAMENTE la información contenida en el siguiente contexto recuperado de la base de datos vectorial.
                Si la respuesta no se encuentra en el contexto proporcionado, responde explícitamente:
                "No dispongo de la información solicitada en los documentos cargados."
                No utilices conocimientos previos externos ni inventes información.
                Además eres un asistente experto en viajes y turismo.
                Responde únicamente utilizando el contexto recuperado desde la base de datos RAG.
                Si no encuentras información suficiente, indícalo claramente: no inventes información y siempre responde en español.
                Además cuando respondas:
                - recomiendas lugar para visitar
                - sugiere actividades turísticas
                - indica la mejor época para viajar
                - también entrega consejos prácticos basados únicamente en la información disponible en los documentos.
                CONTEXTO RECUPERADO:
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

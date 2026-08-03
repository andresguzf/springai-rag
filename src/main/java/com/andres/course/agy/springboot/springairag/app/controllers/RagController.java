package com.andres.course.agy.springboot.springairag.app.controllers;

import com.andres.course.agy.springboot.springairag.app.dto.ChatRequestDto;
import com.andres.course.agy.springboot.springairag.app.dto.ChatResponseDto;
import com.andres.course.agy.springboot.springairag.app.dto.UploadResponseDto;
import com.andres.course.agy.springboot.springairag.app.services.RagService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> uploadPdf(@RequestParam("file") MultipartFile file) {
        UploadResponseDto response = ragService.processAndStorePdf(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(@Valid @RequestBody ChatRequestDto request) {
        ChatResponseDto response = ragService.askQuestion(request);
        return ResponseEntity.ok(response);
    }
}

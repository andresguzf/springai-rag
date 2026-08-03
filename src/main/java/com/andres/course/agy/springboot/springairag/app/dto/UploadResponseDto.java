package com.andres.course.agy.springboot.springairag.app.dto;

public record UploadResponseDto(
    String fileName,
    int totalPages,
    int totalChunksCreated,
    String message
) {}

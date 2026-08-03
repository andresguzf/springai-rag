package com.andres.course.agy.springboot.springairag.app.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(
    @NotBlank(message = "La pregunta no puede estar vacía")
    String message
) {}

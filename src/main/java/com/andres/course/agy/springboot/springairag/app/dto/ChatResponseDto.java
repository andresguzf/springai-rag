package com.andres.course.agy.springboot.springairag.app.dto;

import java.util.List;

public record ChatResponseDto(
    String answer,
    List<String> sources
) {}

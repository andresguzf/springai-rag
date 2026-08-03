package com.andres.course.agy.springboot.springairag.app.services;

import com.andres.course.agy.springboot.springairag.app.dto.ChatRequestDto;
import com.andres.course.agy.springboot.springairag.app.dto.ChatResponseDto;
import com.andres.course.agy.springboot.springairag.app.dto.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface RagService {
    UploadResponseDto processAndStorePdf(MultipartFile file);
    ChatResponseDto askQuestion(ChatRequestDto request);
}

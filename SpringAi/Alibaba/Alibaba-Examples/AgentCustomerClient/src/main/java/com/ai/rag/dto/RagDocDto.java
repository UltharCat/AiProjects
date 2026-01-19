package com.ai.rag.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record RagDocDto(
        MultipartFile ragFile,
        String content,
        String documentNumber
) {
}

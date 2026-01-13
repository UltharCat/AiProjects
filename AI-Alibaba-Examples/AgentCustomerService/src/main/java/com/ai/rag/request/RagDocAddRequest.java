package com.ai.rag.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class RagDocAddRequest {

    /**
     * 上传的文件
     */
    @NotNull(groups = RagDocAddRequest.uploadFile.class)
    private MultipartFile file;

    /**
     * 插入的文本内容
     */
    @NotBlank(groups = RagDocAddRequest.insertContent.class)
    private String content;

    /**
     * 文档业务编号
     */
    @NotBlank(groups = RagDocAddRequest.deleteDocumentByNumber.class)
    private String documentNumber;

    public interface insertContent {}

    public interface uploadFile {}

    public interface deleteDocumentByNumber {}

}

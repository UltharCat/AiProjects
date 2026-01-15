package com.ai.rag.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_rag_document")
public class TRagDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 文档业务编号
     */
    @Column(name = "document_number", nullable = false, length = 200, unique = true)
    private String documentNumber;

    /**
     * 上传的文档名称
     */
    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    /**
     * 上传时间
     */
    @CreationTimestamp
    @Column(name = "upload_time", nullable = false)
    private Instant uploadTime;

    /**
     * 修改时间
     */
    @UpdateTimestamp
    @Column(name = "modify_time", nullable = false)
    private Instant modifyTime;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (uploadTime == null) uploadTime = now;
        if (modifyTime == null) modifyTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        modifyTime = Instant.now();
    }

}


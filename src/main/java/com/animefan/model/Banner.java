package com.animefan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Banner model for homepage slider
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "banners")
public class Banner {

    @Id
    private String id;

    private String title;
    private String description;
    private String imageUrl;
    private String buttonText;
    private String buttonUrl;

    private Integer order;
    private Boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

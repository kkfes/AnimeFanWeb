package com.animefan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Studio entity representing anime production studios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "studios")
public class Studio {

    @Id
    private String id;

    @NotBlank(message = "Studio name is required")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    @Indexed(unique = true)
    private String name;

    private String description;

    private String country;

    private Integer foundedYear;

    private String logoUrl;

    private String website;

    // List of anime IDs produced by this studio
    @Builder.Default
    private List<String> animeIds = new ArrayList<>();

    private Integer animeCount = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

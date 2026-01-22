package com.animefan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for genre statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreStatsDTO {
    private String genre;
    private Long animeCount;
    private Double averageRating;
    private Long totalViews;
    private Long totalFavorites;
}

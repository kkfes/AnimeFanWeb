package com.animefan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for bulk import results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResultDTO {

    private int successCount;
    private int skippedCount;
    private int errorCount;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> importedTitles = new ArrayList<>();

    public void addError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
        errorCount++;
    }

    public void addSuccess(String title) {
        if (importedTitles == null) {
            importedTitles = new ArrayList<>();
        }
        importedTitles.add(title);
        successCount++;
    }

    public void incrementSkipped() {
        skippedCount++;
    }
}

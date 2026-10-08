package com.example.humanit.dto;

import com.example.humanit.model.Document;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record DocumentDto (
        Long id,
        @NotBlank(message = "Document number is required")
        String number,
        String description,
        LocalDate expirationDate
) {
    public Document toEntity() {
        Document document = new Document();
        document.setNumber(this.number);
        document.setDescription(this.description);
        document.setExpirationDate(this.expirationDate);
        return document;
    }
}



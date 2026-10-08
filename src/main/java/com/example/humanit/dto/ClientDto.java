package com.example.humanit.dto;

import com.example.humanit.model.Client;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public record ClientDto(
        Long id,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotBlank(message = "Tax identifier is required")
        String taxIdentifier,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        String phoneNumber,
        @Valid
        List<DocumentDto> documents
) {

    public Client toEntity() {
        Client client = new Client();
        client.setFirstName(this.firstName);
        client.setLastName(this.lastName);
        client.setTaxIdentifier(this.taxIdentifier);
        client.setEmail(this.email);
        client.setPhoneNumber(this.phoneNumber);
        if (this.documents != null) {
            client.setDocuments(this.documents.stream().map(DocumentDto::toEntity).toList());
        } else {
            client.setDocuments(new ArrayList<>());
        }
        return client;
    }
}

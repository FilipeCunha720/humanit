package com.example.humanit.model;

import com.example.humanit.dto.ClientDto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Tax identifier is required")
    @Column(name = "tax_identifier", nullable = false, unique = true, length = 50)
    private String taxIdentifier;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    // CascadeType.ALL + orphanRemoval: saving/deleting a client saves/deletes
    // its documents automatically inside the same transaction.
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    public Client() {
    }

    public Client(String firstName, String lastName, String taxIdentifier, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.taxIdentifier = taxIdentifier;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public void addDocument(Document document) {
        documents.add(document);
        document.setClient(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setClient(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTaxIdentifier() {
        return taxIdentifier;
    }

    public void setTaxIdentifier(String taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents.clear();
        if (documents != null) {
            documents.forEach(this::addDocument);
        }
    }

    public ClientDto toClientDto(){
        return new ClientDto(
                this.id,
                this.firstName,
                this.lastName,
                this.taxIdentifier,
                this.email,
                this.phoneNumber,
                this.documents.stream().map(Document::documentDto).toList()
        );
    }
}

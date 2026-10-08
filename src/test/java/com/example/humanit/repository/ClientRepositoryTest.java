package com.example.humanit.repository;

import com.example.humanit.model.Client;
import com.example.humanit.model.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void savingClient_cascadesToDocuments() {
        Client client = new Client("Anna", "Lee", "TAX-77", "anna@mail.com", "555");
        client.addDocument(new Document("DOC-99", "Visa", LocalDate.of(2032, 3, 3)));

        Client saved = clientRepository.save(client);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDocuments()).allSatisfy(d -> assertThat(d.getId()).isNotNull());
    }

    @Test
    void deletingClient_cascadesToDocuments() {
        Client client = new Client("Bob", "Ray", "TAX-78", "bob@mail.com", null);
        client.addDocument(new Document("DOC-98", "Permit", LocalDate.now()));
        Client saved = clientRepository.save(client);

        clientRepository.deleteById(saved.getId());

        assertThat(clientRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void sampleData_wasSeededByDataSql() {
        Optional<Client> john = clientRepository.findById(1L);
        assertThat(john).isPresent();
        assertThat(john.get().getDocuments()).hasSize(2);
    }
}

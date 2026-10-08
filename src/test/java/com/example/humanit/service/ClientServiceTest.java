package com.example.humanit.service;

import com.example.humanit.exception.ClientNotFoundException;
import com.example.humanit.model.Client;
import com.example.humanit.model.Document;
import com.example.humanit.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void create_persistsClientWithDocuments() {
        Client client = new Client("John", "Doe", "TAX-1", "john@mail.com", "123");
        client.addDocument(new Document("D-1", "Passport", LocalDate.of(2030, 1, 1)));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client saved = clientService.create(client);

        assertThat(saved.getDocuments()).hasSize(1);
        assertThat(saved.getDocuments().get(0).getClient()).isEqualTo(saved);
        verify(clientRepository).save(client);
    }

    @Test
    void findById_returnsClientWhenExists() {
        Client client = new Client("Jane", "Smith", "TAX-2", "jane@mail.com", null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThat(clientService.findById(1L).getEmail()).isEqualTo("jane@mail.com");
    }

    @Test
    void findById_throwsWhenMissing() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(99L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_modifiesAllFieldsAndDocuments() {
        Client existing = new Client("Old", "Name", "TAX-3", "old@mail.com", "000");
        existing.addDocument(new Document("OLD-1", "Old doc", LocalDate.now()));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client updated = new Client("New", "Name", "TAX-4", "new@mail.com", "111");
        updated.addDocument(new Document("NEW-1", "New doc", LocalDate.of(2031, 5, 5)));

        Client result = clientService.update(1L, updated);

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getTaxIdentifier()).isEqualTo("TAX-4");
        assertThat(result.getDocuments()).hasSize(1);
        assertThat(result.getDocuments().get(0).getNumber()).isEqualTo("NEW-1");
        verify(clientRepository).save(existing);
    }

    @Test
    void update_throwsWhenClientDoesNotExist() {
        when(clientRepository.findById(42L)).thenReturn(Optional.empty());
        Client updated = new Client();

        assertThatThrownBy(() -> clientService.update(42L, updated))
                .isInstanceOf(ClientNotFoundException.class);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void delete_removesExistingClient() {
        when(clientRepository.existsById(1L)).thenReturn(true);

        clientService.delete(1L);

        verify(clientRepository).deleteById(1L);
    }

    @Test
    void delete_throwsWhenMissing() {
        when(clientRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> clientService.delete(7L))
                .isInstanceOf(ClientNotFoundException.class);
        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    void findAll_returnsAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(new Client("A", "B", "T-1", "a@b.c", null)));

        assertThat(clientService.findAll()).hasSize(1);
    }
}

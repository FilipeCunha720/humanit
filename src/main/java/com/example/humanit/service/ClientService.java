package com.example.humanit.service;

import com.example.humanit.exception.ClientNotFoundException;
import com.example.humanit.model.Client;
import com.example.humanit.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // @Transactional: client AND its documents are persisted in a single
    // transaction - either everything commits, or nothing does (atomicity).
    @Transactional
    public Client create(Client client) {
        return clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Client findById(Long id) {
        return getClientOrThrow(id);
    }

    @Transactional
    public Client update(Long id, Client updated) {
        Client existing = getClientOrThrow(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setTaxIdentifier(updated.getTaxIdentifier());
        existing.setEmail(updated.getEmail());
        existing.setPhoneNumber(updated.getPhoneNumber());
        // Synchronize documents inside the same transaction: orphanRemoval=true
        // deletes removed documents, CascadeType.ALL inserts the new ones.
        existing.setDocuments(updated.getDocuments());
        return clientRepository.save(existing);
    }

    private Client getClientOrThrow(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Transactional
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }
        clientRepository.deleteById(id);
    }
}

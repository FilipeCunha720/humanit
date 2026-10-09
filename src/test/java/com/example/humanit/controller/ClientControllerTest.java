package com.example.humanit.controller;

import com.example.humanit.exception.ClientNotFoundException;
import com.example.humanit.model.Client;
import com.example.humanit.service.AuthService;
import com.example.humanit.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:client-controller-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String VALID_BODY =
            "{\"firstName\": \"John\", \"lastName\": \"Doe\", "
                    + "\"taxIdentifier\": \"TAX-1001\", \"email\": \"john@example.com\", "
                    + "\"phoneNumber\": \"+1-555-0101\", \"documents\": [{\"number\": \"DOC-1\", "
                    + "\"description\": \"Passport\", \"expirationDate\": \"2030-05-15\"}]}";

    @Test
    void create_returns201WithBody() throws Exception {
        Client saved = new Client("John", "Doe", "TAX-1001", "john@example.com", "+1-555-0101");
        saved.setId(1L);
        when(clientService.create(any(Client.class))).thenReturn(saved);

        mockMvc.perform(post("/clients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void create_returns400WhenInvalid() throws Exception {
        mockMvc.perform(post("/clients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"\", \"email\": \"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returnsList() throws Exception {
        Client c = new Client("Jane", "Smith", "TAX-1002", "jane@example.com", null);
        c.setId(2L);
        when(clientService.findAll()).thenReturn(List.of(c));

        mockMvc.perform(get("/clients").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].lastName").value("Smith"));
    }

    @Test
    void getById_returnsClient() throws Exception {
        Client c = new Client("Jane", "Smith", "TAX-1002", "jane@example.com", null);
        c.setId(2L);
        when(clientService.findById(2L)).thenReturn(c);

        mockMvc.perform(get("/clients/2").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        when(clientService.findById(99L)).thenThrow(new ClientNotFoundException(99L));

        mockMvc.perform(get("/clients/99").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returnsUpdatedClient() throws Exception {
        Client updated = new Client("Johnny", "Doe", "TAX-1001", "johnny@example.com", "+1-555-0101");
        updated.setId(1L);
        when(clientService.update(any(Long.class), any(Client.class))).thenReturn(updated);

        mockMvc.perform(put("/clients/1")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"));
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(clientService).delete(1L);

        mockMvc.perform(delete("/clients/1").with(jwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404WhenMissing() throws Exception {
        doThrow(new ClientNotFoundException(7L)).when(clientService).delete(7L);

        mockMvc.perform(delete("/clients/7").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isUnauthorized());
    }
}

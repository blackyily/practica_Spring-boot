package com.ejemplo.agenda.service;

import com.ejemplo.agenda.dto.ContactRequestDTO;
import com.ejemplo.agenda.dto.ContactResponseDTO;

import java.util.List;

public interface ContactService {

    List<ContactResponseDTO> getAllContactsForCurrentUser();

    ContactResponseDTO getContactByIdForCurrentUser(Long contactId);

    ContactResponseDTO createContactForCurrentUser(ContactRequestDTO request);

    ContactResponseDTO updateContactForCurrentUser(Long contactId, ContactRequestDTO request);

    void deleteContactForCurrentUser(Long contactId);
}

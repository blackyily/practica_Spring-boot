package com.ejemplo.agenda.service.impl;

import com.ejemplo.agenda.dto.ContactRequestDTO;
import com.ejemplo.agenda.dto.ContactResponseDTO;
import com.ejemplo.agenda.exception.ResourceNotFoundException;
import com.ejemplo.agenda.model.Contact;
import com.ejemplo.agenda.model.User;
import com.ejemplo.agenda.repository.ContactRepository;
import com.ejemplo.agenda.service.AuthService;
import com.ejemplo.agenda.service.ContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final AuthService authService;

    public ContactServiceImpl(ContactRepository contactRepository, AuthService authService) {
        this.contactRepository = contactRepository;
        this.authService = authService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponseDTO> getAllContactsForCurrentUser() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return contactRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponseDTO getContactByIdForCurrentUser(Long contactId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Contact contact = contactRepository.findByIdAndUserId(contactId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contacto no encontrado con ID: " + contactId + " para el usuario actual"));
        return mapToResponseDTO(contact);
    }

    @Override
    @Transactional
    public ContactResponseDTO createContactForCurrentUser(ContactRequestDTO request) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        Contact contact = Contact.builder()
                .name(request.getName().trim())
                .phone(request.getPhone().trim())
                .email(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null)
                .address(request.getAddress() != null ? request.getAddress().trim() : null)
                .user(currentUser)
                .build();

        Contact savedContact = contactRepository.save(contact);
        return mapToResponseDTO(savedContact);
    }

    @Override
    @Transactional
    public ContactResponseDTO updateContactForCurrentUser(Long contactId, ContactRequestDTO request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Contact contact = contactRepository.findByIdAndUserId(contactId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contacto no encontrado con ID: " + contactId + " para el usuario actual"));

        contact.setName(request.getName().trim());
        contact.setPhone(request.getPhone().trim());
        contact.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        contact.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);

        Contact updatedContact = contactRepository.save(contact);
        return mapToResponseDTO(updatedContact);
    }

    @Override
    @Transactional
    public void deleteContactForCurrentUser(Long contactId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Contact contact = contactRepository.findByIdAndUserId(contactId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contacto no encontrado con ID: " + contactId + " para el usuario actual"));

        contactRepository.delete(contact);
    }

    private ContactResponseDTO mapToResponseDTO(Contact contact) {
        return ContactResponseDTO.builder()
                .id(contact.getId())
                .name(contact.getName())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .address(contact.getAddress())
                .userId(contact.getUser().getId())
                .ownerUsername(contact.getUser().getUsername())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}

package com.ejemplo.agenda.controller;

import com.ejemplo.agenda.dto.ContactRequestDTO;
import com.ejemplo.agenda.dto.ContactResponseDTO;
import com.ejemplo.agenda.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contactos", description = "Endpoints protegidos para la administración de contactos en la agenda")
@SecurityRequirement(name = "bearerAuth")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "Listar contactos", description = "Obtiene todos los contactos pertenecientes al usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de contactos recuperada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token JWT inválido")
    })
    @GetMapping
    public ResponseEntity<List<ContactResponseDTO>> getAllContacts() {
        List<ContactResponseDTO> contacts = contactService.getAllContactsForCurrentUser();
        return ResponseEntity.ok(contacts);
    }

    @Operation(summary = "Obtener contacto por ID", description = "Recupera un contacto específico asegurando que pertenezca al usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacto encontrado"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDTO> getContactById(
            @Parameter(description = "ID del contacto a consultar") @PathVariable Long id) {
        ContactResponseDTO contact = contactService.getContactByIdForCurrentUser(id);
        return ResponseEntity.ok(contact);
    }

    @Operation(summary = "Crear nuevo contacto", description = "Registra un nuevo contacto asignado al usuario autenticado (Foreign Key user_id).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contacto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<ContactResponseDTO> createContact(@Valid @RequestBody ContactRequestDTO request) {
        ContactResponseDTO created = contactService.createContactForCurrentUser(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar contacto existente", description = "Modifica los datos de un contacto perteneciente al usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContactResponseDTO> updateContact(
            @Parameter(description = "ID del contacto a modificar") @PathVariable Long id,
            @Valid @RequestBody ContactRequestDTO request) {
        ContactResponseDTO updated = contactService.updateContactForCurrentUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar contacto", description = "Elimina un contacto perteneciente al usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteContact(
            @Parameter(description = "ID del contacto a eliminar") @PathVariable Long id) {
        contactService.deleteContactForCurrentUser(id);
        return ResponseEntity.ok(Map.of("message", "Contacto eliminado exitosamente con ID: " + id));
    }
}

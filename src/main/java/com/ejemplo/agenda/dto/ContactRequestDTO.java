package com.ejemplo.agenda.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactRequestDTO {

    public ContactRequestDTO() {
    }

    public ContactRequestDTO(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    @NotBlank(message = "El nombre del contacto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+0-9\\s\\-()]{7,20}$", message = "Formato de teléfono inválido")
    private String phone;

    @Email(message = "Formato de correo electrónico inválido")
    @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
    private String email;

    @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
    private String address;
}

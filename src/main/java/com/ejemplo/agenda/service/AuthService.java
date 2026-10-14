package com.ejemplo.agenda.service;

import com.ejemplo.agenda.dto.AuthResponseDTO;
import com.ejemplo.agenda.dto.LoginRequestDTO;
import com.ejemplo.agenda.dto.RegisterRequestDTO;
import com.ejemplo.agenda.model.User;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);

    User getCurrentAuthenticatedUser();
}

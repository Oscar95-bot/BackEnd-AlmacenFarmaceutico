package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager manager;
    private final JwtService jwt;
    private final UsuarioService usuarios;
    public AuthService(AuthenticationManager manager, JwtService jwt,
                       UsuarioService usuarios) {
        this.manager = manager;
        this.jwt = jwt;
        this.usuarios = usuarios;
    }

    public LoginResponse login(LoginRequest login) {
        var auth = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                UsuarioService.normalizar(login.usuario()), login.contrasena()));
        return jwt.emitir(UsuarioResponse.from(usuarios.actual(auth.getName())));
    }
}

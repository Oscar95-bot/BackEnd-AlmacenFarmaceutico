package com.almacen.farmaceutico.AlmacenFarmaceutico.controller;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final UsuarioService usuarios;
    private final JwtService jwt;

    public AuthController(AuthService auth, UsuarioService usuarios, JwtService jwt) {
        this.auth = auth;
        this.usuarios = usuarios;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public LoginResponse login(@Valid @RequestBody LoginRequest login) {

        return auth.login(login);
    }

    @GetMapping("/perfil")
    public UsuarioResponse perfil(Authentication authentication) {
        return UsuarioResponse.from(usuarios.actual(authentication.getName()));
    }
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication) {
        jwt.revocar((Jwt) authentication.getPrincipal());
    }
}

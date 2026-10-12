package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import java.time.Instant;

public record LoginResponse(String token, String tokenType, Instant expiresAt, UsuarioResponse usuario) {}

package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(@NotBlank @Size(max = 50) String usuario,
                           @NotBlank @Size(max = 72) String contrasena) {}

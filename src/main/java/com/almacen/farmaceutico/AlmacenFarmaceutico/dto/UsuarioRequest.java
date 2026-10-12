package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Rol;
import jakarta.validation.constraints.*;

public record UsuarioRequest(@NotBlank @Size(max = 120) String nombreCompleto,
        @NotBlank @Size(max = 50) String usuario,
        @NotBlank @Size(min = 4, max = 72) String contrasena,
        @NotNull Rol rol) {}

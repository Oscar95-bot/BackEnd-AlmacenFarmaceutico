package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Rol;
import jakarta.validation.constraints.*;

public record UsuarioUpdateRequest(@NotBlank @Size(max = 120) String nombreCompleto,
        @NotNull Rol rol, @NotNull Boolean activo, @Size(min = 4, max = 72) String contrasena) {}

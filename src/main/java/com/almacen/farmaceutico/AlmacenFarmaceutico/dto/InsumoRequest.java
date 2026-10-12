package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InsumoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotNull @Positive Long categoriaId,
        @NotNull @Positive Long unidadMedidaId,
        @NotNull @Positive Long presentacionId,
        @Size(max = 100) String concentracion,
        @PositiveOrZero Integer stockActual,
        @PositiveOrZero Integer stockMinimo,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal precioUnitario,
        @Size(max = 100) String ubicacion,
        @Size(max = 80) String numeroLote,
        @FutureOrPresent LocalDate fechaVencimiento,
        @Positive Long proveedorId) {}

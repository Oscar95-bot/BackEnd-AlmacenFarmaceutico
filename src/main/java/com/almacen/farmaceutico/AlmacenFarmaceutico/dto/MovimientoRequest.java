package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.TipoMovimiento;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record MovimientoRequest(
        @NotNull @Positive Long insumoId,
        @NotNull TipoMovimiento tipo,
        @NotNull @Positive Integer cantidad,
        @NotNull @Positive Long motivoId,
        @Size(max = 150) String origenDestino,
        @Size(max = 80) String numeroGuia,
        @PastOrPresent LocalDate fechaOperacion,
        @Size(max = 500) String observaciones,
        @Size(max = 80) String numeroLote,
        LocalDate fechaVencimiento,
        @Positive Long loteId,
        @Positive Long proveedorId,
        @Positive Long clienteId) {}

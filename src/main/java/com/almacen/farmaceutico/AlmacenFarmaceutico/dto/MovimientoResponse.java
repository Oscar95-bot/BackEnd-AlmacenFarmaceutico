package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Movimiento;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.TipoMovimiento;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimientoResponse(Long id, Long insumoId, String codigoInsumo, String nombreInsumo,
        Long loteId, String numeroLote, TipoMovimiento tipo, int cantidad, LocalDateTime fechaHora,
        LocalDate fechaOperacion, String motivo, String origenDestino, String numeroGuia,
        String observaciones, String responsable, Long motivoId,
        Long proveedorId, String proveedorRuc, String proveedorDireccion,
        Long clienteId, String clienteRuc, String clienteDireccion) {
    public static MovimientoResponse from(Movimiento m) {
        return new MovimientoResponse(m.getId(), m.getInsumo().getId(), m.getCodigoInsumo(),
                m.getNombreInsumo(), m.getLote().getId(), m.getLote().getNumero(), m.getTipo(),
                m.getCantidad(), m.getFechaHora(), m.getFechaOperacion(), m.getMotivo(),
                m.getOrigenDestino(), m.getNumeroGuia(), m.getObservaciones(), m.getResponsable(), m.getMotivoId(),
                m.getProveedorId(), m.getProveedorRuc(), m.getProveedorDireccion(),
                m.getClienteId(), m.getClienteRuc(), m.getClienteDireccion());
    }
}

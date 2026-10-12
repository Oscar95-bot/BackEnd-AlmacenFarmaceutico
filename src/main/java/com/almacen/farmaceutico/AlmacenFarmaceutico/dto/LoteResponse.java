package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Lote;
import java.time.LocalDate;

public record LoteResponse(Long id, Long insumoId, String codigoInsumo, String numero,
        LocalDate fechaVencimiento, int stockActual, String estado) {
    public static LoteResponse from(Lote l) {
        LocalDate hoy = LocalDate.now();
        String estado = l.getFechaVencimiento().isBefore(hoy) ? "Vencido"
                : !l.getFechaVencimiento().isAfter(hoy.plusDays(30)) ? "Por Vencer" : "Vigente";
        return new LoteResponse(l.getId(), l.getInsumo().getId(), l.getInsumo().getCodigo(),
                l.getNumero(), l.getFechaVencimiento(), l.getStockActual(), estado);
    }
}

package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Insumo;
import java.math.BigDecimal;

public record InsumoResponse(Long id, String codigo, String nombre, String categoria,
        String unidadMedida, String presentacion, String concentracion, int stockActual,
        int stockMinimo, BigDecimal precioUnitario, String ubicacion, String proveedor, boolean activo, String estado,
        Long categoriaId, Long unidadMedidaId, Long presentacionId,
        Long proveedorId, String proveedorRuc, String proveedorDireccion) {
    public static InsumoResponse from(Insumo i) {
        return new InsumoResponse(i.getId(), i.getCodigo(), i.getNombre(), i.getCategoria(),
                i.getUnidadMedida(), i.getPresentacion(), i.getConcentracion(), i.getStockActual(),
                i.getStockMinimo(), i.getPrecioUnitario(), i.getUbicacion(), i.getProveedor(), i.isActivo(), i.getEstado(),
                i.getCategoriaId(), i.getUnidadMedidaId(), i.getPresentacionId(),
                i.getProveedorId(), i.getProveedorRuc(), i.getProveedorDireccion());
    }
}

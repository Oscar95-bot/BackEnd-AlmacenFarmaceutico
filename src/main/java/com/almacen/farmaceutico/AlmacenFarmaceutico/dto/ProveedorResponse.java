package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Proveedor;

public record ProveedorResponse(Long id, String nombre, String ruc, String direccion) {
    public static ProveedorResponse from(Proveedor p) {
        return new ProveedorResponse(p.getId(), p.getNombre(), p.getRuc(), p.getDireccion());
    }
}

package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Catalogo;

public record CatalogoResponse(Long id, String clave, String nombre, boolean permiteVencidos) {
    public static CatalogoResponse from(Catalogo c) {
        return new CatalogoResponse(c.getId(), c.getClave(), c.getNombre(), c.isPermiteVencidos());
    }
}
